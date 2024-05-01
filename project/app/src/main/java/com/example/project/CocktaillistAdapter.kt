/**
 * CocktaillistAdapter.kt
 * 칵테일 목록을 표시하는 어댑터를 정의
 * 각 칵테일 항목의 이름과 해당 이미지가 포함
 */


package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CocktaillistAdapter(
    private val cocktail_list: List<String>,
    private val listener: OnCocktailClickListener
) : RecyclerView.Adapter<CocktaillistAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.format_cocktail, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cocktailName = cocktail_list[position]
        holder.cocktailNameTextView.text = cocktailName

        val imageId = holder.itemView.context.resources.getIdentifier(cocktailName, "drawable", holder.itemView.context.packageName)
        holder.cocktailImageView.setImageResource(imageId)

        holder.itemView.setOnClickListener {
            listener.onCocktailClick(cocktailName)
        }
    }
    override fun getItemCount(): Int {
        return cocktail_list.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cocktailNameTextView: TextView = itemView.findViewById(R.id.test_cocktail_name)
        val cocktailImageView: ImageView = itemView.findViewById(R.id.cocktail_img)
    }
}

interface OnCocktailClickListener {
    fun onCocktailClick(name: String)
}