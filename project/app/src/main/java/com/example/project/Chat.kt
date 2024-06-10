/**
 * Chat.kt
 * OpenAI API를 사용하여 칵테일 레시피를 추천하는 챗봇 기능을 구현한 액티비티
 */
package com.example.project

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient;
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import com.aallam.openai.*
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.client.*
import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.assistant.*
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.core.Status
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.message.MessageContent
import com.aallam.openai.api.message.MessageRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.api.run.RunRequest
import com.aallam.openai.api.thread.ThreadId
import kotlinx.coroutines.*
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

class Chat : AppCompatActivity() {
    var recycler_view: RecyclerView? = null
    var tv_welcome: TextView? = null
    var message_edit: EditText? = null
    var send_btn: Button? = null
    var messageList: MutableList<Message>? = null
    var messageAdapter: MessageAdapter? = null
    var client: OkHttpClient = OkHttpClient()
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




    /**
     * 함수 정의 부분
     */

    // 메시지를 보내는 함수
    fun sendMessage(){
        val question = message_edit!!.getText().toString().trim { it <= ' ' }
        addToChat(question, Message.SENT_BY_ME)
//        if (question == "q"){
//            val result = adjustPumps(Companion.recipeString)
//            println(result)
//        }
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

    // OpenAI API를 호출, 사용자 입력에 부합하는 칵테일을 추천한 뒤
    // addResponse 함수를 호출해 응답 메시지로 추가하는 함수
    @OptIn(BetaOpenAI::class)
    suspend fun callAPI(question: String?){
        messageList!!.add(Message("...", Message.SENT_BY_BOT))
        val openai = OpenAI( token = MY_SECRET_KEY)
        val real_user_mood = question

        // Few-shot Learning을 위한 예시 입력 선언
        val exampleInputDict = mapOf(
            "Vodka" to 700, "Rum" to 700, "Gin" to 700, "Diluted Lemon Juice" to 1000,
            "Triple Sec" to 500, "Cranberry Juice" to 1000, "Grapefruit Juice" to 1000, "Orange Juice" to 800
        )

        val realInputDict = mapOf(
            "Vodka" to 1000, "Rum" to 700, "Gin" to 800, "Diluted Lemon Juice" to 800,
            "Triple Sec" to 500, "Cranberry Juice" to 900, "Grapefruit Juice" to 1000, "Orange Juice" to 800
        )

        val exampleUserMood1 = "오늘따라 뭔가 시원하고 달콤한 칵테일이 마시고 싶어. 피로도 풀리고 기분도 좋아지는 그런 종류로."
        val exampleGptResponse1 = "그런 기분에 딱 맞는 칵테일로 '모히토'를 추천드릴게요. 신선한 민트와 라임이 들어가 상큼하고 시원한 맛이 특징이랍니다. 럼 주를 기반으로 해서 달콤한 맛도 느끼실 수 있고요.⇇[{'Rum': 1, 'Diluted Lemon Juice': 2, 'Orange Juice': 3}, {'Rum': 2, 'Diluted Lemon Juice': 3, 'Orange Juice': 4}]"

        val exampleUserMood2 = "오늘 정말 무더운 날이네요. 뭔가 시원하고 상쾌한 음료가 마시고 싶어요."
        val exampleGptResponse2 = "열대의 상쾌함을 원하신다면 '트로피칼 펀치'를 추천드리고 싶어요. 이 칵테일은 자몽 주스와 오렌지 주스를 기반으로 하고 있어서 상큼하고, 럼과 트리플 섹이 더해져 풍미가 풍부합니다. 정말 열대 지방의 분위기를 느낄 수 있죠.⇇[{'Rum': 1, 'Orange Juice': 2, 'Grapefruit Juice': 3, 'Triple Sec': 4, 'Cranberry Juice': 5}, {'Rum': 2, 'Orange Juice': 4, 'Grapefruit Juice': 4, 'Triple Sec': 1, 'Cranberry Juice': 2}]"

        val exampleUserMood3 = "주말 파티를 위해 강렬한 칵테일이 필요해요."
        val exampleGptResponse3 = "주말 파티에는 '마가리타'를 추천드릴게요. 테킬라와 라임 주스, 트리플 섹이 조화를 이루어 강렬하면서도 상쾌한 맛을 느낄 수 있습니다.⇇[{'Tequila': 1, 'Triple Sec': 1, 'Lime Juice': 1}, {'Tequila': 2, 'Triple Sec': 2, 'Lime Juice': 2}]"

        val exampleUserMood4 = "친구와 함께 마실 수 있는 달콤한 칵테일을 추천해줘."
        val exampleGptResponse4 = "친구와 함께 마시기 좋은 칵테일로 '피치 벨리니'를 추천드릴게요. 피치 퓌레와 샴페인이 어우러져 달콤하고 청량한 맛을 느낄 수 있습니다.⇇[{'Peach Puree': 1, 'Champagne': 3}, {'Peach Puree': 1, 'Champagne': 4}]"

        val exampleUserMood5 = "저녁 식사 후에 마실 수 있는 부드러운 칵테일을 원해요."
        val exampleGptResponse5 = "저녁 식사 후에는 '화이트 러시안'을 추천드릴게요. 보드카, 커피 리큐어, 크림이 어우러져 부드럽고 진한 맛을 느낄 수 있습니다.⇇[{'Vodka': 2, 'Coffee Liqueur': 1, 'Cream': 1}, {'Vodka': 3, 'Coffee Liqueur': 2, 'Cream': 2}]"

        // AI 바텐더 Assistant 생성
        val batender = openai.assistant(
            request = AssistantRequest(
                name = "AI Bartender",
                model = ModelId("gpt-4-turbo"),
                instructions = """
                    You are an AI bartender. First, receive the inventory as a dictionary named 'example_dict',
                    then consider the user's mood and preferences to recommend a cocktail.
                    Ensure that the total volume of ingredients does not exceed 250ml. Use a specific delimiter (⇇) to separate the cocktail recommendation from the recipe,
                    which should be provided in a structured list format, including two dictionaries:
                    one for the "Order" of ingredients and another for the "Integer" number of 30ml pumps required for each ingredient.
                """.trimIndent()
            )
        )

        // 대화를 관리할 Thread 생성
        val thread = openai.thread()
        println(thread.id)

        // 사용자 입력을 Thread에 전송
        openai.message(
            threadId = thread.id,
            request = MessageRequest(
                role = Role.User,
                content = real_user_mood!!
            )
        )

        // Thread에 있는 메시지 확인
        val messages = openai.messages(thread.id)
        println("List of messages in the thread:")
        for (message in messages) {
            val textContent = message.content.first() as? MessageContent.Text ?: error("Expected MessageContent.Text")
            println(textContent.text.value)
        }

        // AI 바텐더에게 실행 요청
        val run = openai.createRun(
                threadId = thread.id,
                request = RunRequest(
                    assistantId = batender.id,
                    instructions ="""
                          Example 1:
                            Input: Inventory - $exampleInputDict, Mood/Preference - '$exampleUserMood1'
                            Output: $exampleGptResponse1
                            
                            Example 2:
                            Input: Inventory - $exampleInputDict, Mood/Preference - '$exampleUserMood2'
                            Output: $exampleGptResponse2
                        
                            Example 3:
                            Input: Inventory - $exampleInputDict, Mood/Preference - '$exampleUserMood3'
                            Output: $exampleGptResponse3
                        
                            Example 4:
                            Input: Inventory - $exampleInputDict, Mood/Preference - '$exampleUserMood4'
                            Output: $exampleGptResponse4
                        
                            Example 5:
                            Input: Inventory - $exampleInputDict, Mood/Preference - '$exampleUserMood5'
                            Output: $exampleGptResponse5
                            
                            You have to respond in Korean:
                            Input: Inventory - $realInputDict, Mood/Preference - '$real_user_mood'
                            Output: 
                                            """.trimIndent())
            )

        // 실행 결과가 완료될 때까지 대기
        do {
            delay(1500)
            val retrievedRun = openai.getRun(threadId = thread.id, runId = run.id)
            if (retrievedRun.status != Status.Completed) {
                addResponse("Run Status: ${retrievedRun.status}")
            }
        } while (retrievedRun.status != Status.Completed)

        // AI 바텐더의 응답 메시지 처리
        val assistantMessages = openai.messages(thread.id)
        val message = assistantMessages[0]
        val textContent = message.content.first() as? MessageContent.Text ?: error("Expected MessageContent.Text")
        val messageText = textContent.text.value
        println(messageText)

        if (messageText != null && messageText.isNotEmpty()){
            val parts = messageText.split("⇇")
            if (parts.size > 1) {
                val recommendReason = parts[0]
                val recipeString = parts[1]
                addResponse(recommendReason)
            } else {
                addResponse("Invalid Response: $messageText")
            }
        }else{
            addResponse("Err.. Try again")
        }


    }

    fun adjustPumps(recipeString: String?): Pair<String, Map<String, Int>>? {
        try {
            val recipe = recipeString!!.removeSurrounding("(", ")").split(", ") // 문자열을 분해하여 파싱
            val recipeName = recipe[0].removeSurrounding("'")
            val ingredientsAndPumps = recipe[1].removeSurrounding("{", "}")
                .split(", ")
                .associate {
                    val (ingredient, pumps) = it.split(": ")
                    ingredient.removeSurrounding("'") to pumps.toInt()
                }

            val totalPumps = ingredientsAndPumps.values.sum()
            val targetPumps = 7

            if (totalPumps > targetPumps) {
                addResponse("Adjusting" +
                        " recipe...")
                addResponse("Original recipe: $ingredientsAndPumps")

                val adjustmentRatio = targetPumps.toDouble() / totalPumps
                val adjustedPumps = ingredientsAndPumps.mapValues { (_, pumps) ->
                    (pumps * adjustmentRatio).roundToInt()
                }

                addResponse("Adjusted recipe: $adjustedPumps")
                return Pair(recipeName, adjustedPumps)
            } else {
                return Pair(recipeName, ingredientsAndPumps)
            }
        } catch (e: Exception) {
            addResponse("레시피 파싱 오류: $e")
            return null
        }
    }

    // 클래스 레벨에서 접근 가능한 객체 멤버 선언
    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        private const val MY_SECRET_KEY = ""
        var recipeString: String? = null
    }
}