/**
 * Choose.kt
 * 사용자에게 칵테일을 선택할 수 있는 화면을 제공하는 액티비티
 * select.kt를 호출해 실제 칵테일 선택을 가능하게 한다
 */

package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Choose : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose)

        val recyclerView: RecyclerView = findViewById(R.id.cocktail_list)
        val cocktail_names = listOf(
            "matini",
            "ginfizz",
            "gintonic",
            "orangeblossom",
            "paradise",
            "tomcollins",
            "whitelady"
        )
        val adapter = CocktaillistAdapter(cocktail_names, object : OnCocktailClickListener {
            // select.kt 액티비티 호출
            override fun onCocktailClick(name: String) {
                val intent = Intent(this@Choose, select::class.java)
                intent.putExtra("COCKTAIL_NAME", name)
                startActivity(intent)
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
        

//        val btn = findViewById<Button>(R.id.button)
//        btn.setOnClickListener {
//            val fileContent = readTextFile(this, R.raw.a)
//            val intent = Intent(this, select::class.java).apply {
//                putExtra("fileContent", fileContent)
//            }
//            startActivity(intent)
//        }
    }
}