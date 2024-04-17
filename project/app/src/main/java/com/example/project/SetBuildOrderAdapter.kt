package com.example.project

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class SetBuildAdapter(private var ingredientList: ArrayList<Ingredient>) :
    RecyclerView.Adapter<SetBuildAdapter.IngredientViewHolder>(), ItemMoveCallback {

    // ViewHolder 클래스 정의
    class IngredientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientName: TextView = view.findViewById(R.id.ingredientName)
        val ingredientQuantity: TextView = view.findViewById(R.id.ingredientQuantity)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.format_setbuildorder, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredientList[position]
        holder.ingredientName.text = ingredient.name
        holder.ingredientQuantity.text = ingredient.quantity.toString()
    }

    override fun getItemCount(): Int = ingredientList.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(ingredientList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getItems(): ArrayList<Ingredient>{
        return ingredientList
    }
}

interface ItemMoveCallback {
    fun onItemMove(fromPosition: Int, toPosition: Int)
}


class SimpleItemTouchHelperCallback(val adapter: ItemMoveCallback) : ItemTouchHelper.Callback() {
    override fun isLongPressDragEnabled() = true

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPos = source.adapterPosition
        val toPos = target.adapterPosition
        adapter.onItemMove(fromPos, toPos)
        return true

    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프 삭제 기능은 사용하지 않으므로 구현하지 않음
    }
}

