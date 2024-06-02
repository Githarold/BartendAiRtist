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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        // 버튼 이벤트 설정
        findViewById<Button>(R.id.chooseBtn).setOnClickListener {
            startActivity(Intent(this, Choose::class.java))
        }

        findViewById<Button>(R.id.recommendBtn).setOnClickListener {
            startActivity(Intent(this, Chat::class.java))
        }

        findViewById<Button>(R.id.customBtn).setOnClickListener {
            startActivity(Intent(this, Custom::class.java))
        }

        val devBtn = findViewById<Button>(R.id.devBtn)

        devBtn.setOnClickListener {
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
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(SERVER_UUID)
                    bluetoothSocket.connect()
                    isConnected = true

                    val outStream: OutputStream = bluetoothSocket.outputStream
                    val inStream = bluetoothSocket.inputStream

                    val data = "1"
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

                        runOnUiThread {
                            Toast.makeText(applicationContext, "Data received: $receivedData", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, Dev::class.java).apply {
                                putExtra("receivedData", receivedData)
                            }
                            startActivity(intent)
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
}