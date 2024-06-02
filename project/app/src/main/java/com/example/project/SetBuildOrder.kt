/**
 * SetBuildOrder.kt
 * 사용자에게 빌드 순서를 정하게 하는 액티비티
 * 드래그 앤 드롭으로 빌드 순서를 결정한 뒤 서버에 데이터를 보낸다
 */

package com.example.project

import android.Manifest
import android.content.Intent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.util.UUID

class SetBuildOrder : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val SERVER_DEVICE_ADDRESS = "DC:A6:32:7B:04:EC"  // 서버 기기의 MAC 주소를 입력해야 합니다. 라즈베리파이
    private val SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  // SPP UUID
    private lateinit var bluetoothSocket: BluetoothSocket
    private var isCommunicating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setbuildorder)

        // 블루투스 권한 요청
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth", "Device doesn't support Bluetooth")
            return
        }

        // 블루투스 활성화 요청
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }


        // 시스템 바 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedList = intent.getSerializableExtra("IngredientList") as? ArrayList<Ingredient>

        // 양이 0인 재료(즉, 선택하지 않은 재료)는 제외
        val filteredList = ArrayList(receivedList?.filter { it.quantity > 0 } ?: listOf())

        val adapter = SetBuildAdapter(filteredList)
        val recyclerView: RecyclerView = findViewById(R.id.cocktail_ingredients_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 드래그 앤 드롭 기능
        val callback = SimpleItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val selectBtn = findViewById<Button>(R.id.selectBtn)
        selectBtn.setOnClickListener {
            val currentList = adapter.getItems()
            val ingredientOrderList = getIngredientOrderList(currentList)
            val formattedDataList = getIngredientQuantityList(receivedList)
            val formattedData = formatDataForCommunicationWithOrder(formattedDataList, ingredientOrderList)
            Log.d("formattedData", formattedData)
            sendData(formattedData)
        }
    }



    /**
     * 함수 정의 부분
     */
    fun sendData(data: String){
        synchronized(this) {
            if (isCommunicating) {
                Log.d("Bluetooth", "Communication is already in progress.")
                return@synchronized
            }
            isCommunicating = true
        }

        Thread {
            var isConnected = false
            try {
                val device = bluetoothAdapter.getRemoteDevice(SERVER_DEVICE_ADDRESS)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                        BLUETOOTH_PERMISSION_REQUEST_CODE)
                }
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SERVER_UUID)
                bluetoothSocket.connect()
                isConnected = true

                val outStream: OutputStream = bluetoothSocket.outputStream
                val inStream = bluetoothSocket.inputStream

                val data = data
                outStream.write(data.toByteArray())
                Log.d("Bluetooth", "Data sent: $data")

                // 수신 버퍼 설정
                val buffer = ByteArray(1024)
                val bytesRead = inStream.read(buffer)
                if (bytesRead == -1) {
                    Log.d("Bluetooth", "Peer socket closed")
                } else {
                    val receivedData = String(buffer, 0, bytesRead)
                    Log.d("Bluetooth", "Data received: $receivedData")

                    runOnUiThread{
                        Toast.makeText(applicationContext, "Data received: $receivedData", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (isConnected) {
                    try {
                        bluetoothSocket.close()  // 소켓을 안전하게 닫습니다.
                        Log.d("Bluetooth", "Socket closed")
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                synchronized(this) {
                    isCommunicating = false
                }
            }
        }.start()
    }
}


    /**
     * 함수 정의 부분
     */

// 빌드 순서를 내용으로 하는 리스트를 만드는 함수
fun getIngredientOrderList(currentList: List<Ingredient>): String {
    val stringBuilder = StringBuilder()
    val totalIngredients = 8 // 전체 재료 개수
    for (ingredient in currentList) {
        stringBuilder.append("${ingredient.quantity}\n")
    }
    // 나머지 요소는 빈 줄로 채움
    val remainingEmptyLines = totalIngredients - currentList.size
    repeat(remainingEmptyLines) {
        stringBuilder.append("0\n")
    }
    return stringBuilder.toString()
}

// 서버로 보내기 위해 데이터 형식을 알밪게 바꿔주는 함수
fun getIngredientQuantityList(ingredients: ArrayList<Ingredient>?): String {
    //TODO
    val header = "3" // 순서 포함하므로 헤드 3

    val body = StringBuilder()

    // ingredients는 항상 ex1에서 ex8까지 고정된 순서로 있다고 가정
    val expectedIngredients = listOf("ex1", "ex2", "ex3", "ex4", "ex5", "ex6", "ex7", "ex8")
    expectedIngredients.forEach { ingredientName ->
        val quantity = ingredients?.find { it.name == ingredientName }?.quantity ?: 0
        body.append("$quantity\n")
    }

    return "$header\n\n${body.toString()}"
}

// 서버로 보내기 위해 데이터 형식을 알밪게 바꿔주는 함수(순서 포함)
fun formatDataForCommunicationWithOrder(receivedList: String, ingredientOrderList: String): String {
    val formattedData = StringBuilder()

    formattedData.append(receivedList)

    // ingredientOrderList를 추가
    formattedData.append("\n\n") // '\n\n' 추가
    formattedData.append(ingredientOrderList)

    return formattedData.toString()
}