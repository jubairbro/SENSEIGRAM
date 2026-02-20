package com.senseigram.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class AppTheme {
    LIGHT, DARK, AMOLED, SYSTEM
}

enum class ColorAccent(val displayName: String, val colorRes: Int) {
    EMERALD("Emerald", 0xFF10B981.toInt()),
    BLUE("Blue", 0xFF3B82F6.toInt()),
    VIOLET("Violet", 0xFF8B5CF6.toInt()),
    ROSE("Rose", 0xFFF43F5E.toInt()),
    AMBER("Amber", 0xFFF59E0B.toInt())
}

enum class MediaType {
    TEXT, PHOTO, VIDEO, DOCUMENT
}

@Parcelize
data class TelegramUser(
    val id: Long,
    val is_bot: Boolean,
    val first_name: String,
    val username: String? = null,
    val can_join_groups: Boolean = false,
    val can_read_all_group_messages: Boolean = false,
    val supports_inline_queries: Boolean = false
) : Parcelable

@Parcelize
data class TelegramChat(
    val id: Long,
    val title: String? = null,
    val username: String? = null,
    val type: String,
    val photo: ChatPhoto? = null
) : Parcelable

@Parcelize
data class ChatPhoto(
    val small_file_id: String,
    val big_file_id: String
) : Parcelable

@Parcelize
data class SavedChat(
    val id: Long,
    val title: String,
    val type: String,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class InlineButton(
    val text: String,
    val url: String? = null,
    val callback_data: String? = null
) : Parcelable

@Parcelize
data class MessageDraft(
    val id: String,
    val chatId: String,
    val html: String,
    val buttons: List<List<InlineButton>>,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class Announcement(
    val show: Boolean = false,
    val title: String = "",
    val text: String = "",
    val buttonText: String? = null,
    val buttonUrl: String? = null
) : Parcelable

data class RemoteConfig(
    val api_id: String? = null,
    val api_hash: String? = null
)

data class SendMessageResult(
    val success: Boolean,
    val messageId: Long? = null,
    val error: String? = null
)
