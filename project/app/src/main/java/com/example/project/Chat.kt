package com.example.project

import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.databinding.ActivityChatBinding

class Chat : AppCompatActivity() {

    private lateinit var receiverName: String
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageList: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 키보드가 활성화될 때 레이아웃 조정
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        receiverName = "쌈뽕한 바텐더"
        supportActionBar?.title = receiverName

        messageList = ArrayList()
        val messageAdapter: MessageAdapter = MessageAdapter(this, messageList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        binding.sendBtn.setOnClickListener {
            val messageText = binding.messageEdit.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message(messageText)
                messageList.add(message)
                messageAdapter.notifyItemInserted(messageList.size - 1)
                binding.messageEdit.setText("")
                binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
            }
        }
    }
}
