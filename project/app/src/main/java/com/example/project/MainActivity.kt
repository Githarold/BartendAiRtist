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
        val chooseBtn = findViewById<Button>(R.id.chooseBtn)
        chooseBtn.setOnClickListener {
            val intent = Intent(this, Choose::class.java)
            startActivity(intent)
        }
        val recommendBtn = findViewById<Button>(R.id.recommendBtn)
        recommendBtn.setOnClickListener {
            val intent = Intent(this, Chat::class.java)
            startActivity(intent)
        }
        val customBtn = findViewById<Button>(R.id.customBtn)
        customBtn.setOnClickListener {
            val intent = Intent(this, Custom::class.java)
            startActivity(intent)
        }
    }
}