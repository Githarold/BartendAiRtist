package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredientAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.nameTextView.text = ingredient.name
        holder.quantityTextView.text = ingredient.quantity.toString()

        holder.plusButton.setOnClickListener {
            ingredient.quantity++
            notifyItemChanged(position)
        }

        holder.minusButton.setOnClickListener {
            if (ingredient.quantity > 0) {
                ingredient.quantity--
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = ingredients.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.ingredientName)
        val quantityTextView: TextView = itemView.findViewById(R.id.how_many)
        val plusButton: Button = itemView.findViewById(R.id.plus)
        val minusButton: Button = itemView.findViewById(R.id.minus)
    }
}

