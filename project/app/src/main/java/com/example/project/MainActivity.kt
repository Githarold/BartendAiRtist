/**
 * MainActivity.kt
 * 앱의 메인 화면을 담당하는 액티비티
 * 사용자는 버튼으로 Choose(칵테일 선택), Chat(칵테일 추천받기), Custom(칵테일 커스텀하기) 중 하나의 액티비티를 선택할 수 있다
 */

package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 칵테일 선택하기
        val chooseBtn = findViewById<Button>(R.id.chooseBtn)
        chooseBtn.setOnClickListener {
            val intent = Intent(this, Choose::class.java)
            startActivity(intent)
        }

        // 칵테일 추천받기
        val recommendBtn = findViewById<Button>(R.id.recommendBtn)
        recommendBtn.setOnClickListener {
            val intent = Intent(this, Chat::class.java)
            startActivity(intent)
        }

        // 칵테일 커스텀하기
        val customBtn = findViewById<Button>(R.id.customBtn)
        customBtn.setOnClickListener {
            val intent = Intent(this, Custom::class.java)
            startActivity(intent)
        }
    }
}