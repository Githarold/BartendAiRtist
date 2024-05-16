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

    fun sendMessage(){
        val question = message_edit!!.getText().toString().trim { it <= ' ' }
        addToChat(question, Message.SENT_BY_ME)
        callAPI(question)
        message_edit!!.text.clear()
        tv_welcome!!.setVisibility(View.GONE)
    }

    fun addToChat(message: String?, sentBy: String?) {
        runOnUiThread {
            messageList!!.add(Message(message, sentBy))
            messageAdapter!!.notifyDataSetChanged()
            recycler_view!!.smoothScrollToPosition(messageAdapter!!.getItemCount())
        }
    }

    fun addResponse(response: String?) {
        messageList!!.removeAt(messageList!!.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    fun callAPI(question: String?) {
        //okhttp
        val prompt = generate_cocktail_recipe(question.toString())

        messageList!!.add(Message("...", Message.SENT_BY_BOT))
        val obj = JSONObject()
        try {
            obj.put("model", "gpt-3.5-turbo")
            obj.put("messages", JSONArray().put(JSONObject().put("role", "system").put("content", prompt))) // 객체 배열로 변경
            obj.put("max_tokens", 500)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val body = obj.toString().toRequestBody(JSON)

//        println(prompt)
        val request: Request = Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + MY_SECRET_KEY)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    println(response)
                    var jsonObject: JSONObject? = null
                    try {
                        val responseBody = response.body
                        val responseBodyString = responseBody?.string()
                        jsonObject = JSONObject(responseBodyString)
                        var jsonArray = jsonObject?.getJSONArray("choices")
                        val result = jsonArray?.getJSONObject(0)?.getString("message")
                        val content = JSONObject(result).getString("content")
                        addResponse(content?.trim { it <= ' ' })
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body?.string())
                }
            }

        })
    }

    companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        private const val MY_SECRET_KEY = ""
    }
}

fun generate_cocktail_recipe(userMood: String?): String{
    val example1 = mapOf(
        "input" to "기분이 우울할 때",
        "output" to mapOf(
            "name" to "블루 라군",
            "ingredients" to listOf("보드카", "블루 큐라소", "레몬 주스", "소다"),
            "reason" to "상쾌한 맛이 기분을 전환시켜 줍니다."
        )
    )

    val example2 = mapOf(
        "input" to "기분이 행복할 때",
        "output" to mapOf(
            "name" to "마가리타",
            "ingredients" to listOf("데킬라", "트리플 섹", "라임 주스"),
            "reason" to "새콤달콤한 맛이 기쁜 기분을 더욱 돋보이게 합니다."
        )
    )

    var prompt = "Input: ${example1["input"]}\nOutput: ${Gson().toJson(example1["output"])}\n\n"
    prompt += "Input: ${example2["input"]}\nOutput: ${Gson().toJson(example2["output"])}\n\n"
    prompt += "Input: $userMood\nOutput:"
    return prompt

}