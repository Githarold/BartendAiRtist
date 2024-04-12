package com.example.project

data class Ingredient(var name: String, var quantity: Int = 0) {
    fun getNameAndQuantity(): Pair<String, Int> {
        return Pair(name, quantity)
    }
}