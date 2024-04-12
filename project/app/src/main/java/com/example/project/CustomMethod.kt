package com.example.project

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class CustomMethod(private val ingredientsList: List<Ingredient>) : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choosecustommethod)

        val buildBtn = findViewById<Button>(R.id.buildBtn)
        buildBtn.setOnClickListener{
            val (ingredientAName, ingredientAQuantity) = ingredientsList[0].getNameAndQuantity()
        }

        val stiringBtn = findViewById<Button>(R.id.stiringBtn)
        stiringBtn.setOnClickListener {
            finish()
        }

    }
}