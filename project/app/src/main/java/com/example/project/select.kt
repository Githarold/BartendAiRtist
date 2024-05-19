package com.example.project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

class select : AppCompatActivity() {
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
        val recipe = jsonObject.getString("recipe")

        val cocktailImg = findViewById<ImageView>(R.id.cocktail_img)
        val imageId = resources.getIdentifier(cocktailName, "drawable", packageName)
        cocktailImg.setImageResource(imageId)

        val cocktailTextView = findViewById<TextView>(R.id.textView)
        cocktailTextView.text = description

        val cocktailNameView = findViewById<TextView>(R.id.cocktail_name)
        cocktailNameView.text = name

        val selectBtn = findViewById<Button>(R.id.select_cocktail)

        selectBtn.setOnClickListener {
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

                    val data = recipe
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

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}

fun readTextFile(context: Context, fileName: String): String {
    val resourceId = context.resources.getIdentifier(fileName, "raw", context.packageName)
    if (resourceId == 0) {
        throw IllegalArgumentException("The given file name does not correspond to a resource in raw folder.")
    }

    context.resources.openRawResource(resourceId).use { inputStream ->
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            content.append(line)
            line = reader.readLine()
        }
        return content.toString()
    }
}
