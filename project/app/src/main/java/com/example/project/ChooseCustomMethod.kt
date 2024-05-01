/**
 * ChooseCustomMethod.kt
 * 사용자에게 빌드, 스터링 중 원하는 커스텀 방식을 고르게 하는 액티비티
 * 스터링 선택 경우: 커스텀 데이터를 서버로 전송
 * 빌드 선택 경우: SetBuildOrder.kt 호출, 빌드 순서를 설정하게 함
 */

package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.Socket


class ChooseCustomMethod : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choosecustommethod)

        val receivedList = intent.getSerializableExtra("IngredientList",) as? ArrayList<Ingredient>
        // 통신을 위해 커스텀 한 데이터를 통신 데이터 형식에 알맞게 변환
        val formattedData = formatDataForCommunication(receivedList)

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
            connectToServer(formattedData)
        }
    }



    /**
     * 함수 정의 부분
     */

    private fun  connectToServer(dataToSend:String){
        Thread {
            try {
                val socket = Socket("10.0.2.2", 3000)
                socket.use { s ->
                    val outStream = s.outputStream
                    val inStream = s.inputStream

                    val data = dataToSend
                    outStream.write(data.toByteArray())

                    // 데이터 수신을 위한 버퍼 준비
                    val dataArr = ByteArray(1024)
                    val numBytes = inStream.read(dataArr)
                    if (numBytes != -1) {
                        val receivedData = String(dataArr, 0, numBytes)
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