package com.example.project

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Custom : AppCompatActivity() {

    private var totalQuantity = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_custom)

        // RecyclerView를 찾고 어댑터 설정
        val recyclerView: RecyclerView = findViewById(R.id.cocktail_ingredients_list)
        val ingredientsList = listOf(
            Ingredient("a"),
            Ingredient("b"),
            Ingredient("c"),
            Ingredient("d"),
            Ingredient("e"),
            Ingredient("f"),
            Ingredient("g"),
            Ingredient("h"),
            Ingredient("i"),
            Ingredient("j"),
        )
        val adapter = IngredientAdapter(ingredientsList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 시스템 바 인셋 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val selectBtn = findViewById<Button>(R.id.selectBtn)
        selectBtn.setOnClickListener {
            calculateTotalQuantity(ingredientsList)
            if (totalQuantity > 10) {
                showWarningDialog()
            } else {
                showConfirmationDialog()
            }
        }
    }

    // 경고 다이얼로그 표시
    private fun showWarningDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("경고")
        builder.setMessage("재료의 양이 너무 많습니다. \n 컵이 넘칠 수 있습니다.")
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss() // 다이얼로그 닫기
        }
        val dialog = builder.create()
        dialog.show()
    }

    // 확인 다이얼로그 표시
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("칵테일 만들기")
        builder.setMessage("해당 재료로 칵테일을 만드시겠습니까?")
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss() // 다이얼로그 닫기
            // 칵테일 만들기 동작을 수행
            val intent = Intent(this, CustomMethodChoose::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("취소") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss() // 다이얼로그 닫기
        }
        val dialog = builder.create()
        dialog.show()
    }

    // 재료의 양의 총 합 계산
    private fun calculateTotalQuantity(ingredientsList: List<Ingredient>) {
        totalQuantity = 0
        ingredientsList.forEach { ingredient ->
            totalQuantity += ingredient.quantity
        }
    }
}
