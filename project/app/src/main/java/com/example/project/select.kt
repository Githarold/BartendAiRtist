package com.example.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.io.*

class select : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val cocktailName = intent.getStringExtra("COCKTAIL_NAME") ?: return

        val cocktailImg = findViewById<ImageView>(R.id.cocktail_img)
        val imageId = resources.getIdentifier(cocktailName, "drawable", packageName)

        val content = readTextFile(this, cocktailName)
        cocktailImg.setImageResource(imageId)
        val cocktailTextView = findViewById<TextView>(R.id.textView)
        cocktailTextView.text = content
        val cocktailNameView = findViewById<TextView>(R.id.cocktail_name)
        cocktailNameView.text = cocktailName
        val selectBtn = findViewById<Button>(R.id.select_cocktail)
        selectBtn.setOnClickListener {
            Thread {
                try {
                    val socket = Socket("10.0.2.2", 3000)
                    socket.use { s ->
                        val outStream = s.outputStream
                        val inStream = s.inputStream

//                        val data = "1"
//                        val data = "2\n\n2\n3\n4\n5\n6\n7"
                        val data = "3\n\n2\n3\n4\n5\n6\n7\n\n3\n4\n5\n"
//                        val data = "4\n\n2\n3\n4\n5\n6\n7"
                        outStream.write(data.toByteArray())

                        // 데이터 수신을 위한 버퍼 준비
                        val dataArr = ByteArray(1024)  // 적당한 크기의 버퍼 설정
                        val numBytes = inStream.read(dataArr)  // 서버로부터 데이터 읽기, 데이터 도착까지 블록됨
                        if (numBytes != -1) {  // 데이터가 정상적으로 읽혔는지 확인
                            val receivedData = String(dataArr, 0, numBytes)  // 읽은 바이트 수만큼 문자열로 변환
                            runOnUiThread {
                                println("data : $receivedData")
                            }
                        } else {
                            println("No data received")
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
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