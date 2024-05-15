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
        messageList!!.add(Message("...", Message.SENT_BY_BOT))
        val `object` = JSONObject()
        try {
            `object`.put("model", "gpt-3.5-turbo")
            `object`.put("messages", JSONArray().put(JSONObject().put("role", "system").put("content", question))) // 객체 배열로 변경
            `object`.put("max_tokens", 4000)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val body = `object`.toString().toRequestBody(JSON)
//        println(body)
        val request: Request = Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + MY_SECRET_KEY)
            .post(body)
            .build()
//        println(request)
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
//                    println(response)
                    var jsonObject: JSONObject? = null
                    try {
                        val responseBody = response.body
                        val responseBodyString = responseBody?.string()
                        jsonObject = JSONObject(responseBodyString)
                        println(jsonObject)
                        val jsonArray = jsonObject?.getJSONArray("choices")
                        println(jsonArray)
                        val result = jsonArray?.getJSONObject(0)?.getString("message")
                        addResponse(result?.trim { it <= ' ' })
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
        private const val MY_SECRET_KEY = "우리 api key"
    }
}

