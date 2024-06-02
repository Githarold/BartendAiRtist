/**
 * ChooseCustomMethod.kt
 * 사용자에게 빌드, 스터링 중 원하는 커스텀 방식을 고르게 하는 액티비티
 * 스터링 선택 경우: 커스텀 데이터를 서버로 전송
 * 빌드 선택 경우: SetBuildOrder.kt 호출, 빌드 순서를 설정하게 함
 */

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.util.UUID


class ChooseCustomMethod : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val SERVER_DEVICE_ADDRESS = "DC:A6:32:7B:04:EC"  // 서버 기기의 MAC 주소를 입력해야 합니다. 라즈베리파이
    //    private val SERVER_DEVICE_ADDRESS = "E0:0A:F6:49:E5:1C"
    private val SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  // SPP UUID
    private lateinit var bluetoothSocket: BluetoothSocket
    private var isCommunicating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choosecustommethod)

        // 블루투스 권한 요청
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                BLUETOOTH_PERMISSION_REQUEST_CODE)
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

        val receivedList = intent.getSerializableExtra("IngredientList",) as? ArrayList<Ingredient>
        // 통신을 위해 커스텀 한 데이터를 통신 데이터 형식에 알맞게 변환
        val formattedData: String = formatDataForCommunication(receivedList)

        val buildBtn = findViewById<Button>(R.id.buildBtn)
        // buildBtn 누르면 빌드 순서 커스텀 화면으로 넘어감
        buildBtn.setOnClickListener {
            val intent = Intent(this, SetBuildOrder::class.java)
            intent.putExtra("IngredientList", ArrayList<Ingredient>(receivedList))
            startActivity(intent)
        }

        val stiringBtn = findViewById<Button>(R.id.stiringBtn)
        // stiringBtn 누르면 바로 서버로 보냄
        stiringBtn.setOnClickListener {
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

//서버로 보내기 위해 데이터 형식을 알밪게 바꿔주는 함수
//리스트 형태의 데이터를 받아 String으로 바꿔 리턴
fun formatDataForCommunication(ingredients: ArrayList<Ingredient>?): String {
    //TODO
    val header = "2" // 일단은 2로 고정

    val body = StringBuilder()

    // ingredients는 항상 ex1에서 ex8까지 고정된 순서로 있다고 가정
    val expectedIngredients = listOf("ex1", "ex2", "ex3", "ex4", "ex5", "ex6", "ex7", "ex8")
    expectedIngredients.forEach { ingredientName ->
        val quantity = ingredients?.find { it.name == ingredientName }?.quantity ?: 0
        body.append("$quantity\n")
    }

    return "$header\n\n${body.toString()}"
}