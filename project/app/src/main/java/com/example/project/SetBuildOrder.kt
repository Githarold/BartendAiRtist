package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

class SetBuildOrder : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
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

        val adapter = SetBuildOrderAdapter(filteredList)
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
            val builder = AlertDialog.Builder(this)
            builder.setMessage("해당 칵테일을 제조하시겠습니까?")
                .setPositiveButton("예") { dialog, id ->
                    sendData(formattedData)
                    Log.d("formattedData", formattedData)
                }
                .setNegativeButton("아니오") { dialog, id ->
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    /**
     * 함수 정의 부분
     */
    fun sendData(data: String) {
        synchronized(this) {
            if (isCommunicating) {
                Log.d("Bluetooth", "Communication is already in progress.")
                return@synchronized
            }
            isCommunicating = true
        }

        val communicationThread = Thread {
            try {
                val socket = BluetoothManager.getBluetoothSocket()
                if (socket == null || !socket.isConnected) {
                    runOnUiThread {
                        Toast.makeText(this, "Not connected to any device", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                val outStream: OutputStream = socket.outputStream
                outStream.write(data.toByteArray())
                Log.d("Bluetooth", "Data sent: $data")

                // 수신 버퍼 설정
                val buffer = ByteArray(1024)
                val inStream = socket.inputStream
                val bytesRead = inStream.read(buffer)
                if (bytesRead == -1) {
                    Log.d("Bluetooth", "Peer socket closed")
                } else {
                    val receivedData = String(buffer, 0, bytesRead)
                    Log.d("Bluetooth", "Data received: $receivedData")

                    runOnUiThread {
                        Toast.makeText(applicationContext, "Data received: $receivedData", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                synchronized(this) {
                    isCommunicating = false
                }
                Thread.currentThread().interrupt()
            }
        }
        communicationThread.start()
    }
}

/**
 * 함수 정의 부분
 */

// 빌드 순서를 내용으로 하는 리스트를 만드는 함수
fun getIngredientOrderList(currentList: List<Ingredient>): String {
    val totalIngredients = 8 // 전체 재료 개수
    val orderList = MutableList(totalIngredients) { 0 }

    for ((index, ingredient) in currentList.withIndex()) {
        val position = ingredient.order?: continue
        if (position in 1..totalIngredients) {
            orderList[position - 1] = index + 1
        }
    }

    return orderList.joinToString(separator = "\n")
}


// 서버로 보내기 위해 데이터 형식을 알맞게 바꿔주는 함수
fun getIngredientQuantityList(ingredients: ArrayList<Ingredient>?): String {
    val header = "3" // 순서 포함하므로 헤드 3

    val body = StringBuilder()

    // ingredients는 항상 ex1에서 ex8까지 고정된 순서로 있다고 가정
    val expectedIngredients = listOf("Vodka", "Rum", "Gin", "Triple Sec", "Diluted Lemon Syrup", "Orange Juice", "Grapefruit Juice", "Cranberry Juice")
    expectedIngredients.forEach { ingredientName ->
        val quantity = ingredients?.find { it.name == ingredientName }?.quantity ?: 0
        body.append("$quantity\n")
    }

    return "$header\n\n${body.toString()}"
}

// 서버로 보내기 위해 데이터 형식을 알맞게 바꿔주는 함수(순서 포함)
fun formatDataForCommunicationWithOrder(receivedList: String, ingredientOrderList: String): String {
    val formattedData = StringBuilder()

    formattedData.append(receivedList)

    // ingredientOrderList를 추가
    formattedData.append("\n") // '\n\n' 추가
    formattedData.append(ingredientOrderList)

    return formattedData.toString()
}
