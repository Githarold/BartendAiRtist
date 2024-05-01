package com.example.project

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.databinding.ActivityChatBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat



class Chat : AppCompatActivity() {

    private lateinit var receiverName: String
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageList: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 키보드가 활성화될 때 레이아웃 조정
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        receiverName = "쌈뽕한 바텐더"
        supportActionBar?.title = receiverName

        messageList = ArrayList()
        val messageAdapter: MessageAdapter = MessageAdapter(this, messageList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        // 메시지 입력창에서 엔터 키를 누르거나 전송 버튼을 클릭하면 sendMessage() 호출
        binding.messageEdit.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    sendMessage()
                    return true
                }
                return false
            }
        })
        // 전송 버튼을 클릭하면 sendMessage() 호출
        binding.sendBtn.setOnClickListener {
            sendMessage()
        }
    }


    /**
     * 함수 정의 부분
     */
    // 메시지 전송 메서드
    private fun sendMessage() {
        val messageText = binding.messageEdit.text.toString()
        if (messageText.isNotEmpty()) {
            val message = Message(messageText)
            messageList.add(message)
            (binding.chatRecyclerView.adapter as? MessageAdapter)?.notifyItemInserted(messageList.size - 1)
            binding.messageEdit.setText("")
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }
}
