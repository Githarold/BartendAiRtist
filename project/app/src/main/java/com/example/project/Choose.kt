package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.OutputStream

class Choose : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var isCommunicating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth", "Device doesn't support Bluetooth")
            return
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                BLUETOOTH_PERMISSION_REQUEST_CODE)
        }

        val recyclerView: RecyclerView = findViewById(R.id.cocktail_list)
        val cocktail_names = listOf(
            "seabreeze",
            "baybreeze",
            "planterspunch",
            "screwdriver",
            "hurricane",
            "greyhound",
            "cosmopolitan",
            "reddevil",
            "whitelady",
            "longbeachicedtea",
            "lemondropmartini"
        )
        val adapter = CocktaillistAdapter(this, cocktail_names, object : OnCocktailClickListener {
            override fun onCocktailClick(name: String) {
                sendDataAndProceed(name)
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun sendDataAndProceed(cocktailName: String) {
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
                val data = "1"
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
                        val intent = Intent(this@Choose, select::class.java).apply {
                            putExtra("COCKTAIL_NAME", cocktailName)
                            putExtra("RECEIVED_DATA", receivedData)
                        }
                        startActivity(intent)
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
