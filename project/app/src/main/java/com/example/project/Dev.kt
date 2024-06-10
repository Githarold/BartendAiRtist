package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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

class Dev : AppCompatActivity() {
    private var totalQuantity = 0
    private val ingredientsList = listOf(
        Ingredient("ex1"),
        Ingredient("ex2"),
        Ingredient("ex3"),
        Ingredient("ex4"),
        Ingredient("ex5"),
        Ingredient("ex6"),
        Ingredient("ex7"),
        Ingredient("ex8"),
    )
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var isCommunicating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_custom)

        // 시스템 바 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent로부터 데이터를 받음
        val receivedData = intent.getStringExtra("receivedData")
        if (receivedData != null) {
            val quantities = receivedData.split("\n")
            if (quantities.size == ingredientsList.size) {
                for (i in ingredientsList.indices) {
                    ingredientsList[i].quantity = quantities[i].toIntOrNull() ?: 0
                }
            } else {
                Toast.makeText(this, "데이터의 크기가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "수신된 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
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

        // RecyclerView를 찾고 어댑터 설정
        val recyclerView: RecyclerView = findViewById(R.id.cocktail_ingredients_list)
        val adapter = IngredientAdapter(ingredientsList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val selectBtn = findViewById<Button>(R.id.selectBtn)
        selectBtn.setOnClickListener {
            synchronized(this) {
                if (isCommunicating) {
                    Log.d("Bluetooth", "Communication is already in progress.")
                    return@synchronized
                }
                isCommunicating = true
            }

            Thread {
                try {
                    val socket = BluetoothManager.getBluetoothSocket()
                    if (socket == null || !socket.isConnected) {
                        runOnUiThread {
                            Toast.makeText(this, "Not connected to any device", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }

                    val outStream: OutputStream = socket.outputStream

                    // ingredientsList의 quantity 값을 문자열로 변환하고 '\n'으로 연결
                    val data = "4\n\n" + ingredientsList.joinToString(separator = "\n") { it.quantity.toString() }
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

                        // 수신된 데이터를 UI 스레드에서 토스트 메시지로 표시
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Complete", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    synchronized(this) {
                        isCommunicating = false
                    }
                }
            }.start()
        }
    }
}
