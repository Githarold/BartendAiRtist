/**
 * MessageAdapter.kt
 * 앱에서 채팅 메시지를 표시하기 위한 RecyclerView 어댑터 클래스
 */
package com.example.project

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.OutputStream


class MessageAdapter(private val messageList: List<Message>, private val chatInstance: Chat) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var isCommunicating = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.send, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        val recipe = Chat.Companion.recipeString
        if (message.sentBy == Message.SENT_BY_ME) {
            holder.leftChatView.visibility = View.GONE
            holder.rightChatView.visibility = View.VISIBLE
            holder.rightChatTv.text = message.message
            holder.selectBtn.visibility = View.GONE
        } else {
            holder.rightChatView.visibility = View.GONE
            holder.leftChatView.visibility = View.VISIBLE
            holder.leftChatTv.text = message.message

            if (message.hasButton) {
                holder.selectBtn.visibility = View.VISIBLE
            } else {
                holder.selectBtn.visibility = View.GONE
            }
        }

        holder.selectBtn.setOnClickListener {
            if (recipe != null && recipe.isNotEmpty()) {
                makeCusotmcocktail(holder.itemView.context, recipe)
            } else {
                Toast.makeText(holder.itemView.context, "Recipe not available", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int = messageList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatView: LinearLayout = itemView.findViewById(R.id.received_chat_view)
        val rightChatView: LinearLayout = itemView.findViewById(R.id.send_chat_view)
        val leftChatTv: TextView = itemView.findViewById(R.id.receivedMsg)
        val rightChatTv: TextView = itemView.findViewById(R.id.sendMsg)
        val selectBtn: Button = itemView.findViewById(R.id.select_button)
    }

    fun makeCusotmcocktail(context: Context, recipe: String){
        val purerecipe = recipeExplain(recipe)
        val message = "칵테일을 만드시겠습니까?\n\n$purerecipe"
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setPositiveButton("확인") { dialog, _ ->
            chatInstance.sendData(recipe)
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun recipeExplain(recipe: String): String{
        val parts = recipe.split("\n\n")
        val purerecipe = parts[1]
        val ingredients = purerecipe.split("\n").map { it.toInt() }
        val cocktailNames = listOf("Vodka", "Rum", "Gin", "Triple Sec", "Diluted Lemon Syrup", "Orange Juice", "Grapefruit Juice", "Cranberry Juice")
        val result = StringBuilder()

        // 각 칵테일에 대한 수량을 순회하며 문자열을 생성합니다.
        for (i in ingredients.indices) {
            val amount = ingredients[i]
            if (amount > 0) { // 수량이 0보다 큰 경우에만 결과 문자열에 추가합니다.
                val cocktailName = cocktailNames.getOrNull(i) ?: "Unknown"
                result.append("$cocktailName: $amount\n")
            }
        }

        return result.toString().trim()
    }
}

