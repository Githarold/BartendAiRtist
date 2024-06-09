package com.example.project

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.IOException

class CocktaillistAdapter(
    private val context: Context,
    private val cocktail_list: List<String>,
    private val listener: OnCocktailClickListener
) : RecyclerView.Adapter<CocktaillistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.format_cocktail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cocktailName = cocktail_list[position]

        // JSON 파일에서 ename을 추출하여 설정
        val fileName = "$cocktailName.json"
        val json = try {
            context.assets.open(fileName).reader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }

        val ename = if (json.isNotEmpty()) {
            val jsonObject = JSONObject(json)
            jsonObject.getString("ename")
        } else {
            cocktailName
        }

        holder.cocktailNameTextView.text = ename

        val imageId = context.resources.getIdentifier(cocktailName, "drawable", context.packageName)
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
