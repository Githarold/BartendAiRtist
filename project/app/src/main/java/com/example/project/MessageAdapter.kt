/**
 * MessageAdapter.kt
 * 앱에서 채팅 메시지를 표시하기 위한 RecyclerView 어댑터 클래스
 */
package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MessageAdapter(private val messageList: List<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.send, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        if (message.sentBy == Message.SENT_BY_ME) {
            holder.leftChatView.visibility = View.GONE
            holder.rightChatView.visibility = View.VISIBLE
            holder.rightChatTv.text = message.message
        } else {
            holder.rightChatView.visibility = View.GONE
            holder.leftChatView.visibility = View.VISIBLE
            holder.leftChatTv.text = message.message
        }
    }

    override fun getItemCount(): Int = messageList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatView: LinearLayout = itemView.findViewById(R.id.received_chat_view)
        val rightChatView: LinearLayout = itemView.findViewById(R.id.send_chat_view)
        val leftChatTv: TextView = itemView.findViewById(R.id.receivedMsg)
        val rightChatTv: TextView = itemView.findViewById(R.id.sendMsg)
    }
}