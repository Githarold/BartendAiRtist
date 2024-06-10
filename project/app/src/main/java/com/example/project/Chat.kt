package com.example.project

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.*
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.ThreadId
import com.aallam.openai.client.*
import com.aallam.openai.client.OpenAI

class Chat : AppCompatActivity() {
    var recycler_view: RecyclerView? = null
    var tv_welcome: TextView? = null
    var message_edit: EditText? = null
    var send_btn: Button? = null
    var messageList: MutableList<Message>? = null
    var messageAdapter: MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        recycler_view = findViewById<RecyclerView>(R.id.chat_recyclerView)
        tv_welcome = findViewById<TextView>(R.id.tv_welcome)
        message_edit = findViewById<EditText>(R.id.message_edit)
        send_btn = findViewById<Button>(R.id.send_btn)
        recycler_view!!.setHasFixedSize(true)
        val manager = LinearLayoutManager(this)
        manager.setStackFromEnd(true)
        recycler_view!!.setLayoutManager(manager)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(messageList!!)
        recycler_view!!.setAdapter(messageAdapter)

        send_btn!!.setOnClickListener(View.OnClickListener {
            sendMessage()
        })

        message_edit!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                sendMessage()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }

    // 메시지를 보내는 함수
    fun sendMessage(){
        val question = message_edit!!.getText().toString().trim { it <= ' ' }
        addToChat(question, Message.SENT_BY_ME)
        CoroutineScope(Dispatchers.Main).launch {
            callAPI(question)
        }
        message_edit!!.text.clear()
        tv_welcome!!.setVisibility(View.GONE)
    }

    // 채팅 메시지를 추가하는 함수
    fun addToChat(message: String?, sentBy: String?) {
        runOnUiThread {
            messageList!!.add(Message(message, sentBy))
            messageAdapter!!.notifyDataSetChanged()
            recycler_view!!.smoothScrollToPosition(messageAdapter!!.getItemCount())
        }
    }

    // 응답 메시지를 추가하는 함수
    fun addResponse(response: String?) {
        messageList!!.removeAt(messageList!!.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    // OpenAI API를 호출하여 사용자 입력에 부합하는 칵테일을 추천한 뒤
    // addResponse 함수를 호출해 응답 메시지로 추가하는 함수
    @OptIn(BetaOpenAI::class)
    suspend fun callAPI(question: String?) {
        messageList!!.add(Message("...", Message.SENT_BY_BOT))
        val openai = OpenAI(token = MY_SECRET_KEY)
        val real_user_mood = question

        val assistant = openai.assistant(
            request = AssistantRequest(
                name = "AI Bartender",
                model = ModelId("gpt-4-turbo"),
                instructions = """
                    You are an AI bartender. You will receive the user's mood and preferences, and based on that,
                    you will recommend a cocktail from the following list:
                    씨 브리즈, 베이 브리즈, 플랜터즈 펀치, 스크류 드라이버, 허리케인, 그레이 하운드, 코스모폴리탄, 레드 데빌, 화이트 레이디, 롱 비치 아이스티, 레몬 드롭 마티니.

                    Here are the ingredients and quantities for each cocktail:
                    - 씨 브리즈: {1,0,0,0,0,0,3,2}, {2,0,0,0,0,0,1,4}
                    - 베이 브리즈: {1,0,0,0,0,2,0,3}, {2,0,0,0,0,1,0,4}
                    - 플랜터즈 펀치: {0,1,0,0,0,2,3,0}, {0,2,0,0,0,2,1,0}
                    - 스크류 드라이버: {1,0,0,0,0,2,0,0}, {2,0,0,0,0,3,0,0}
                    - 허리케인: {0,2,0,0,0,2,0,1}
                    - 그레이 하운드: {2,0,0,0,0,0,0,3}
                    - 코스모폴리탄: {2,0,0,1,0,0,0,1}
                    - 레드 데빌: {2,0,0,1,0,0,0,3}
                    - 화이트 레이디: {0,0,2,1,0,0,0,1}
                    - 롱 비치 아이스티: {1,1,1,1,0,0,3,0}
                    - 레몬 드롭 마티니: {2,0,0,1,0,0,0,1}

                    Each index in the material_list and quantity_list corresponds to the following ingredients:
                    1. 보드카
                    2. 럼
                    3. 진
                    4. 트리플 섹
                    5. 희석된 레몬 원액
                    6. 오렌지 주스
                    7. 자몽주스
                    8. 크랜베리 주스

                    Ensure that the cocktail matches the user's preferences and mood. Provide the recommendation in Korean.
                """.trimIndent()
            )
        )

        val thread = openai.thread()

        openai.message(
            threadId = thread.id,
            request = MessageRequest(
                role = Role.User,
                content = real_user_mood!!
            )
        )

        val run = openai.createRun(
            threadId = thread.id,
            request = RunRequest(
                assistantId = assistant.id,
                instructions = """
                    Mood/Preference - '$real_user_mood'
                    Output: 
                    Select one of these cocktails: 씨 브리즈, 베이 브리즈, 플랜터즈 펀치, 스크류 드라이버, 허리케인, 그레이 하운드, 코스모폴리탄, 레드 데빌, 화이트 레이디, 롱 비치 아이스티, 레몬 드롭 마티니.
                """.trimIndent()
            )
        )

        do {
            delay(1500)
            val retrievedRun = openai.getRun(threadId = thread.id, runId = run.id)
            if (retrievedRun.status != Status.Completed) {
                addResponse("Run Status: ${retrievedRun.status}")
            }
        } while (retrievedRun.status != Status.Completed)

        val assistantMessages = openai.messages(thread.id)
        val message = assistantMessages.firstOrNull()?.content?.firstOrNull() as? MessageContent.Text
        val messageText = message?.text?.value ?: "Err.. Try again"

        addResponse(messageText)
    }

    companion object {
        private const val MY_SECRET_KEY = ""
    }
}