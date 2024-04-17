package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.databinding.ActivityChatBinding

class Chat : AppCompatActivity() {

    private  lateinit var receiverName: String

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageList: ArrayList<Message>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        messageList = ArrayList()
        val messageAdapter: MessageAdapter = MessageAdapter(this,messageList)

        binding.chatRecyclerView.layoutManager=LinearLayoutManager(this)
        binding.chatRecyclerView.adapter=messageAdapter

        setContentView(binding.root)
        receiverName = "쌈뽕한 바텐더"
        supportActionBar?.title = receiverName
        messageList.clear()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.sendBtn.setOnClickListener{
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