package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CocktaillistAdapter(
    private val cock_list: List<String>,
    private val listener: OnCocktailClickListener
) : RecyclerView.Adapter<CocktaillistAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.format_cocktail, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = cock_list[position]
        holder.nameTextView.text = name

        val imageId = holder.itemView.context.resources.getIdentifier(name, "drawable", holder.itemView.context.packageName)
        holder.cocktailImageView.setImageResource(imageId)

        holder.itemView.setOnClickListener {
            listener.onCocktailClick(name)
        }
    }
    override fun getItemCount(): Int {
        return cock_list.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.test_cocktail_name)
        val cocktailImageView: ImageView = itemView.findViewById(R.id.img_cocktail)
    }
}

interface OnCocktailClickListener {
    fun onCocktailClick(name: String)
}