package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream

class select : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var isCommunicating = false
    private var receivedDataList: List<Int> = listOf()
    private var recipeDataList: List<Int> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select)

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cocktailName = intent.getStringExtra("COCKTAIL_NAME") ?: return
        val fileName = "$cocktailName.json"
        val json = try {
            assets.open(fileName).reader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            return  // 파일이 없거나 읽을 수 없는 경우 함수를 종료합니다.
        }
        val jsonObject = JSONObject(json)

        val name = jsonObject.getString("name")
        val description = jsonObject.getString("description")
        val head = jsonObject.getString("head")
        val recipe = jsonObject.getString("recipe")
        val order = jsonObject.getString("order")

        val receivedData = intent.getStringExtra("RECEIVED_DATA") ?: ""
        receivedDataList = processData(receivedData)
        recipeDataList = processData(recipe)

        Log.d("SelectActivity", "Received Data List: $receivedDataList")
        Log.d("SelectActivity", "Recipe Data List: $recipeDataList")

        val cocktailImg = findViewById<ImageView>(R.id.cocktail_img)
        val imageId = resources.getIdentifier(cocktailName, "drawable", packageName)
        cocktailImg.setImageResource(imageId)

        val cocktailTextView = findViewById<TextView>(R.id.textView)
        cocktailTextView.text = description

        val cocktailNameView = findViewById<TextView>(R.id.cocktail_name)
        cocktailNameView.text = name

        val selectBtn = findViewById<Button>(R.id.select_cocktail)

        if (!validateData(receivedDataList, recipeDataList)) {
            selectBtn.isEnabled = false
        }
        else {
            selectBtn.isEnabled = true
        }

        val send_data = "$head\n\n$recipe\n\n$order"
        selectBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("해당 칵테일을 제조하시겠습니까?")
                .setPositiveButton("예") { dialog, id ->
                    sendData(send_data)
                    Log.d("recipe", send_data)
                }
                .setNegativeButton("아니오") { dialog, id ->
                    dialog.dismiss()
                }
            builder.create().show()
        }

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }

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

    private fun processData(data: String): List<Int> {
        return data.split("\n").mapNotNull { it.toIntOrNull() }
    }

    private fun validateData(receivedData: List<Int>, recipeData: List<Int>): Boolean {
        if (receivedData.size != recipeData.size) return false
        for (i in receivedData.indices) {
            if (receivedData[i] - recipeData[i] <= 2 && recipeData[i]>0) {
                return false
            }
        }
        return true
    }
}
