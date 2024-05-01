/**
 * Ingredient.kt
 * Ingredient 데이터 클래스를 정의
 * 다른 구성 요소 간에 데이터를 전달하고 전송하기 위해 Serializable 인터페이스를 구현
 * 이를 통해 객체를 직렬화하여 전송 가능한 형식으로 변환할 수 있다
 */

package com.example.project
import java.io.Serializable

data class Ingredient(var name: String, var quantity: Int = 0) : Serializable