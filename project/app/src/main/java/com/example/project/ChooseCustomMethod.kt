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
import java.io.IOException
import java.io.OutputStream

class ChooseCustomMethod : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
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

        val receivedList = intent.getSerializableExtra("IngredientList") as? ArrayList<Ingredient>
        // 통신을 위해 커스텀 한 데이터를 통신 데이터 형식에 알맞게 변환
        val formattedData: String = formatDataForCommunication(receivedList)

        val buildBtn = findViewById<Button>(R.id.buildBtn)
        // buildBtn 누르면 빌드 순서 커스텀 화면으로 넘어감
        buildBtn.setOnClickListener {
            val intent = Intent(this, SetBuildOrder::class.java)
            intent.putExtra("IngredientList", ArrayList(receivedList))
            startActivity(intent)
        }

        val stiringBtn = findViewById<Button>(R.id.stiringBtn)
        // stiringBtn 누르면 바로 서버로 보냄
        stiringBtn.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("해당 칵테일을 제조하시겠습니까?")
                    .setPositiveButton("예") { dialog, id ->
                        sendData("$formattedData\n0\n0\n0\n0\n0\n0\n0\n0")
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

//서버로 보내기 위해 데이터 형식을 알맞게 바꿔주는 함수
//리스트 형태의 데이터를 받아 String으로 바꿔 리턴
fun formatDataForCommunication(ingredients: ArrayList<Ingredient>?): String {
    val header = "2" // 일단은 2로 고정

    val body = StringBuilder()

    // ingredients는 항상 ex1에서 ex8까지 고정된 순서로 있다고 가정
    val expectedIngredients = listOf("Vodka", "Rum", "Gin", "Triple Sec", "Diluted Lemon Syrup", "Orange Juice", "Grapefruit Juice", "Cranberry Juice")
    expectedIngredients.forEach { ingredientName ->
        val quantity = ingredients?.find { it.name == ingredientName }?.quantity ?: 0
        body.append("$quantity\n")
    }

    return "$header\n\n${body.toString()}"
}
