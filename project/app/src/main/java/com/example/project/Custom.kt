/**
 * Custom.kt
 * 재료 목록을 보여준다. 사용자는 + 버튼과 - 버튼으로 각 재료의 양을 커스텀할 수 있다.
 * 만약 재료의 총합이 컵 용량(현재 total<10으로 임의 설정)을 넘어서면
 * 재료가 넘칠 수 있으니 주의하라는 경고 다이얼로그를 표시하고 다음 액티비티로 넘어갈 수 없다.
 *
 * 재료의 총합이 컵 용량 이내일 경우 ChooseCustomMethod.kt 액티비티를 호출해
 * 사용자가 커스텀 방법(빌드, 스터링)을 선택하게 한다
 */

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
    val ingredientsList = listOf(
        Ingredient("Vodka", order = 1 ),
        Ingredient("Rum", order = 2),
        Ingredient("Gin", order = 3),
        Ingredient("Triple Sec", order = 4),
        Ingredient("Diluted Lemon Syrup", order = 5),
        Ingredient("Orange Juice", order = 6),
        Ingredient("Grapefruit Juice", order = 7),
        Ingredient("Cranberry Juice", order = 8),
    )
    val maxCount = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_custom)


        // RecyclerView를 찾고 어댑터 설정
        val recyclerView: RecyclerView = findViewById(R.id.cocktail_ingredients_list)
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

        // 선택 버튼을 누른 경우, totalQuantity가 7(210mL)을 넘으면 경고 다이얼로그 표시
        // 10(임의 값)을 넘지 않는다면 ChooseCustomMethod.kt 호출
        val selectBtn = findViewById<Button>(R.id.selectBtn)
        selectBtn.setOnClickListener {
            calculateTotalQuantity(ingredientsList)
            if (totalQuantity > maxCount) {
                showWarningDialog()
            } else {
                showConfirmationDialog()
            }
        }

        val noticeBtn = findViewById<Button>(R.id.noticeBtn)
        noticeBtn.setOnClickListener{
            showCustomUnitDialog()
        }
    }



    /**
     * 다이얼로그 표시 or 함수 정의 부분
     */

    // 경고 다이얼로그 표시
    private fun showWarningDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("경고")
        builder.setMessage("재료의 양이 너무 많습니다. \n컵이 넘칠 수 있습니다.")
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
            dialogInterface.dismiss()
            // ChooseCustomMethod.kt 호출
            val intent = Intent(this, ChooseCustomMethod::class.java)
            intent.putExtra("IngredientList", ArrayList<Ingredient>(ingredientsList))
            startActivity(intent)
        }
        builder.setNegativeButton("취소") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
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

    private fun showCustomUnitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("커스텀 단위")
        builder.setMessage("경고: 한 펌프는 30mL입니다.\n재료의 총 펌프 수가 7(=210mL)을 넘으면 \n컵이 넘칠 수 있습니다!")
        builder.setPositiveButton("확인") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
