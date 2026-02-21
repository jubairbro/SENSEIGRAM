package com.senseigram.data

data class Bot(val id: Long, val name: String, val username: String?)
data class Chat(val id: Long, val title: String?, val username: String?, val type: String)
data class SavedChat(val id: Long, val title: String, val type: String, val time: Long = System.currentTimeMillis())

data class InlineBtn(
    val text: String,
    val url: String? = null,
    val callback: String? = null,
    val style: String = "default" // default, primary, success, danger
)

data class MessageDraft(
    val chatId: String,
    val text: String,
    val buttons: List<List<InlineBtn>>
)
