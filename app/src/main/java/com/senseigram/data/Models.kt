package com.senseigram.data

data class BotInfo(
    val token: String,
    val firstName: String,
    val username: String?
)

data class ChatInfo(
    val id: Long,
    val title: String,
    val type: String
)

data class SavedChat(
    val id: Long,
    val title: String,
    val type: String
)
