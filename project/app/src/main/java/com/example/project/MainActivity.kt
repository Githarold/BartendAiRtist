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
import com.example.project.Pairing.Companion.selectedDeviceAddress
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var SERVER_DEVICE_ADDRESS: String? = null
    private var isCommunicating = false
    private var communicationThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        findViewById<Button>(R.id.chooseBtn).setOnClickListener {
            startActivity(Intent(this, Choose::class.java))
        }

        findViewById<Button>(R.id.recommendBtn).setOnClickListener {
            startActivity(Intent(this, Chat::class.java))
        }

        findViewById<Button>(R.id.customBtn).setOnClickListener {
            startActivity(Intent(this, Custom::class.java))
        }
        findViewById<Button>(R.id.PairingBtn).setOnClickListener {
            startActivity(Intent(this, Pairing::class.java))
        }

        val devBtn = findViewById<Button>(R.id.devBtn)

        devBtn.setOnClickListener {
            SERVER_DEVICE_ADDRESS = selectedDeviceAddress
            if (SERVER_DEVICE_ADDRESS == null) {
                Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            synchronized(this) {
                if (isCommunicating) {
                    Log.d("Bluetooth", "Communication is already in progress.")
                    return@synchronized
                }
                isCommunicating = true
            }

            communicationThread = Thread {
                try {
                    val socket = BluetoothManager.getBluetoothSocket()
                    if (socket == null || !socket.isConnected) {
                        val device = bluetoothAdapter.getRemoteDevice(SERVER_DEVICE_ADDRESS)
                        BluetoothManager.connectToDevice(this, device)
                    }
                    val outStream: OutputStream = BluetoothManager.getBluetoothSocket()!!.outputStream

                    val data = "1"
                    outStream.write(data.toByteArray())
                    Log.d("Bluetooth", "Data sent: $data")

                    // 수신 버퍼 설정
                    val buffer = ByteArray(1024)
                    val inStream = BluetoothManager.getBluetoothSocket()!!.inputStream
                    val bytesRead = inStream.read(buffer)
                    if (bytesRead == -1) {
                        Log.d("Bluetooth", "Peer socket closed")
                    } else {
                        val receivedData = String(buffer, 0, bytesRead)
                        Log.d("Bluetooth", "Data received: $receivedData")

                        runOnUiThread {
                            Toast.makeText(applicationContext, "DevOption", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, Dev::class.java).apply {
                                putExtra("receivedData", receivedData)
                            }
                            startActivity(intent)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    synchronized(this) {
                        isCommunicating = false
                    }
                    communicationThread?.interrupt()
                }
            }
            communicationThread?.start()
        }
    }

    override fun onResume() {
        super.onResume()
        SERVER_DEVICE_ADDRESS = selectedDeviceAddress
    }

    override fun onPause() {
        super.onPause()
        // 스레드를 중지합니다.
        communicationThread?.interrupt()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 스레드를 중지합니다.
        communicationThread?.interrupt()
    }
}
