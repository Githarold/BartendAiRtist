/**
 * Message.kt
 * 앱에서 사용되는 메시지 객체를 나타내는 Kotlin 클래스.
 * 사용자와 봇 간의 메시지를 표현하는 데 사용된다.
 */
package com.example.project

class Message {
    var message: String? = null
    var sentBy: String? = null
    var hasButton: Boolean = false

    constructor(message: String?, sentBy: String?, hasButton: Boolean = false) {
        this.message = message
        this.sentBy = sentBy
        this.hasButton = hasButton
    }

    constructor()

    companion object {
        var SENT_BY_ME = "me"
        var SENT_BY_BOT = "bot"
    }
}