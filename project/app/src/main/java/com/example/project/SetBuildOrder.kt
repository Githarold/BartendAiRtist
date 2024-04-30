package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.net.Socket

class SetBuildOrder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setbuildorder)

        // 시스템 바 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedList = intent.getSerializableExtra("IngredientList") as? ArrayList<Ingredient>

        // 양이 0인 재료(즉, 선택하지 않은 재료)는 제외
        val filteredList = ArrayList(receivedList?.filter { it.quantity > 0 } ?: listOf())

        val adapter = SetBuildAdapter(filteredList)
        val recyclerView: RecyclerView = findViewById(R.id.cocktail_ingredients_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 드래그 앤 드롭 기능
        val callback = SimpleItemTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val selectBtn = findViewById<Button>(R.id.selectBtn)
        selectBtn.setOnClickListener {
            val currentList = adapter.getItems()
            val ingredientOrderList = getIngredientOrderList(currentList)
            val formattedDataList = formatDataForCommunicationWithOrder(receivedList)
            val formattedData = formatDataWithOrder(formattedDataList, ingredientOrderList)
            println(formattedData)
            connectToServer(formattedData)
        }
    }


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

fun getIngredientOrderList(currentList: List<Ingredient>): String {
    val stringBuilder = StringBuilder()
    val totalIngredients = 8 // 전체 재료 개수
    for (ingredient in currentList) {
        stringBuilder.append("${ingredient.quantity}\n")
    }
    // 나머지 요소는 빈 줄로 채움
    val remainingEmptyLines = totalIngredients - currentList.size
    repeat(remainingEmptyLines) {
        stringBuilder.append("0\n")
    }
    return stringBuilder.toString()
}

fun formatDataWithOrder(receivedList: String, ingredientOrderList: String): String {
    val formattedData = StringBuilder()

    formattedData.append(receivedList)

    // ingredientOrderList를 추가
    formattedData.append("\n\n") // '\n\n' 추가
    formattedData.append(ingredientOrderList)

    return formattedData.toString()
}

fun formatDataForCommunicationWithOrder(ingredients: ArrayList<Ingredient>?): String {
    //TODO
    val header = "3" // 일단은 3으로 고정

    val body = StringBuilder()

    // ingredients는 항상 ex1에서 ex8까지 고정된 순서로 있다고 가정
    val expectedIngredients = listOf("ex1", "ex2", "ex3", "ex4", "ex5", "ex6", "ex7", "ex8")
    expectedIngredients.forEach { ingredientName ->
        val quantity = ingredients?.find { it.name == ingredientName }?.quantity ?: 0
        body.append("$quantity\n")
    }

    return "$header\n\n${body.toString()}"
}