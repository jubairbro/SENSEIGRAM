package com.senseigram.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TelegramUser(
    val id: Long,
    val is_bot: Boolean,
    val first_name: String,
    val username: String? = null
)

@Serializable
data class TelegramChat(
    val id: Long,
    val title: String? = null,
    val username: String? = null,
    val type: String
)

@Serializable
data class TelegramResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    val description: String? = null
)

@Serializable
data class SendMessageResult(
    val message_id: Long,
    val chat: TelegramChat
)

data class SavedChat(
    val id: Long,
    val title: String,
    val type: String,
    val addedAt: Long = System.currentTimeMillis()
)
