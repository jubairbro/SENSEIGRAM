package com.senseigram.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.senseigram.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class TelegramService(private val token: String) {
    
    companion object {
        private const val BASE_URL = "https://api.telegram.org/bot"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val gson = Gson()
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private suspend fun <T> request(method: String, body: RequestBody? = null, parser: (JsonObject) -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL$token/$method"
                val requestBuilder = Request.Builder().url(url)
                
                if (body != null) {
                    requestBuilder.post(body)
                } else {
                    requestBuilder.post("{}".toRequestBody(JSON_MEDIA_TYPE))
                }
                
                val response = client.newCall(requestBuilder.build()).execute()
                val responseBody = response.body?.string() ?: "{}"
                val json = gson.fromJson(responseBody, JsonObject::class.java)
                
                if (json.get("ok")?.asBoolean == true) {
                    Result.success(parser(json))
                } else {
                    val error = json.get("description")?.asString ?: "Unknown error"
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getMe(): Result<TelegramUser> {
        return request("getMe") { json ->
            val result = json.getAsJsonObject("result")
            TelegramUser(
                id = result.get("id").asLong,
                is_bot = result.get("is_bot")?.asBoolean ?: false,
                first_name = result.get("first_name").asString,
                username = result.get("username")?.asString,
                can_join_groups = result.get("can_join_groups")?.asBoolean ?: false,
                can_read_all_group_messages = result.get("can_read_all_group_messages")?.asBoolean ?: false,
                supports_inline_queries = result.get("supports_inline_queries")?.asBoolean ?: false
            )
        }
    }
    
    suspend fun getChat(chatId: String): Result<TelegramChat> {
        val body = FormBody.Builder()
            .add("chat_id", chatId)
            .build()
        
        return request("getChat", body) { json ->
            val result = json.getAsJsonObject("result")
            TelegramChat(
                id = result.get("id").asLong,
                title = result.get("title")?.asString,
                username = result.get("username")?.asString,
                type = result.get("type").asString,
                photo = result.getAsJsonObject("photo")?.let {
                    ChatPhoto(
                        small_file_id = it.get("small_file_id").asString,
                        big_file_id = it.get("big_file_id").asString
                    )
                }
            )
        }
    }
    
    suspend fun sendMessage(
        chatId: String,
        text: String,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null,
        silent: Boolean = false,
        protect: Boolean = false,
        disableWebPreview: Boolean = false
    ): Result<Long> {
        val jsonBody = JsonObject().apply {
            addProperty("chat_id", chatId)
            addProperty("text", text)
            addProperty("parse_mode", parseMode)
            addProperty("disable_notification", silent)
            addProperty("protect_content", protect)
            addProperty("disable_web_page_preview", disableWebPreview)
            
            if (!buttons.isNullOrEmpty()) {
                val keyboard = buildInlineKeyboard(buttons)
                add("reply_markup", keyboard)
            }
        }
        
        return request("sendMessage", jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)) { json ->
            json.getAsJsonObject("result").get("message_id").asLong
        }
    }
    
    suspend fun sendPhoto(
        chatId: String,
        photo: File,
        caption: String? = null,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null,
        silent: Boolean = false,
        protect: Boolean = false,
        hasSpoiler: Boolean = false
    ): Result<Long> {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("photo", photo.name, photo.asRequestBody("image/*".toMediaType()))
            .apply {
                caption?.let { addFormDataPart("caption", it) }
                addFormDataPart("parse_mode", parseMode)
                if (silent) addFormDataPart("disable_notification", "true")
                if (protect) addFormDataPart("protect_content", "true")
                if (hasSpoiler) addFormDataPart("has_spoiler", "true")
                
                if (!buttons.isNullOrEmpty()) {
                    addFormDataPart("reply_markup", gson.toJson(buildInlineKeyboard(buttons)))
                }
            }
            .build()
        
        return request("sendPhoto", body) { json ->
            json.getAsJsonObject("result").get("message_id").asLong
        }
    }
    
    suspend fun sendPhotoByUrl(
        chatId: String,
        photoUrl: String,
        caption: String? = null,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null,
        silent: Boolean = false,
        protect: Boolean = false,
        hasSpoiler: Boolean = false
    ): Result<Long> {
        val jsonBody = JsonObject().apply {
            addProperty("chat_id", chatId)
            addProperty("photo", photoUrl)
            caption?.let { addProperty("caption", it) }
            addProperty("parse_mode", parseMode)
            if (silent) addProperty("disable_notification", true)
            if (protect) addProperty("protect_content", true)
            if (hasSpoiler) addProperty("has_spoiler", true)
            
            if (!buttons.isNullOrEmpty()) {
                add("reply_markup", buildInlineKeyboard(buttons))
            }
        }
        
        return request("sendPhoto", jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)) { json ->
            json.getAsJsonObject("result").get("message_id").asLong
        }
    }
    
    suspend fun sendVideo(
        chatId: String,
        video: File,
        caption: String? = null,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null,
        silent: Boolean = false,
        protect: Boolean = false,
        hasSpoiler: Boolean = false
    ): Result<Long> {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("video", video.name, video.asRequestBody("video/*".toMediaType()))
            .apply {
                caption?.let { addFormDataPart("caption", it) }
                addFormDataPart("parse_mode", parseMode)
                if (silent) addFormDataPart("disable_notification", "true")
                if (protect) addFormDataPart("protect_content", "true")
                if (hasSpoiler) addFormDataPart("has_spoiler", "true")
                
                if (!buttons.isNullOrEmpty()) {
                    addFormDataPart("reply_markup", gson.toJson(buildInlineKeyboard(buttons)))
                }
            }
            .build()
        
        return request("sendVideo", body) { json ->
            json.getAsJsonObject("result").get("message_id").asLong
        }
    }
    
    suspend fun sendDocument(
        chatId: String,
        document: File,
        caption: String? = null,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null,
        silent: Boolean = false,
        protect: Boolean = false
    ): Result<Long> {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("document", document.name, document.asRequestBody("*/*".toMediaType()))
            .apply {
                caption?.let { addFormDataPart("caption", it) }
                addFormDataPart("parse_mode", parseMode)
                if (silent) addFormDataPart("disable_notification", "true")
                if (protect) addFormDataPart("protect_content", "true")
                
                if (!buttons.isNullOrEmpty()) {
                    addFormDataPart("reply_markup", gson.toJson(buildInlineKeyboard(buttons)))
                }
            }
            .build()
        
        return request("sendDocument", body) { json ->
            json.getAsJsonObject("result").get("message_id").asLong
        }
    }
    
    suspend fun editMessageText(
        chatId: String,
        messageId: Long,
        text: String,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null
    ): Result<Boolean> {
        val jsonBody = JsonObject().apply {
            addProperty("chat_id", chatId)
            addProperty("message_id", messageId)
            addProperty("text", text)
            addProperty("parse_mode", parseMode)
            
            if (buttons != null) {
                add("reply_markup", buildInlineKeyboard(buttons))
            }
        }
        
        return request("editMessageText", jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)) {
            true
        }
    }
    
    suspend fun editMessageCaption(
        chatId: String,
        messageId: Long,
        caption: String,
        parseMode: String = "HTML",
        buttons: List<List<InlineButton>>? = null
    ): Result<Boolean> {
        val jsonBody = JsonObject().apply {
            addProperty("chat_id", chatId)
            addProperty("message_id", messageId)
            addProperty("caption", caption)
            addProperty("parse_mode", parseMode)
            
            if (buttons != null) {
                add("reply_markup", buildInlineKeyboard(buttons))
            }
        }
        
        return request("editMessageCaption", jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)) {
            true
        }
    }
    
    suspend fun editMessageReplyMarkup(
        chatId: String,
        messageId: Long,
        buttons: List<List<InlineButton>>
    ): Result<Boolean> {
        val jsonBody = JsonObject().apply {
            addProperty("chat_id", chatId)
            addProperty("message_id", messageId)
            add("reply_markup", buildInlineKeyboard(buttons))
        }
        
        return request("editMessageReplyMarkup", jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE)) {
            true
        }
    }
    
    private fun buildInlineKeyboard(buttons: List<List<InlineButton>>): JsonObject {
        val keyboard = gson.toJsonTree(buttons.map { row ->
            row.map { btn ->
                mapOf(
                    "text" to btn.text,
                    btn.url?.let { "url" to it },
                    btn.callback_data?.let { "callback_data" to it }
                ).filterValues { it != null }
            }
        }).asJsonArray
        
        return JsonObject().apply {
            add("inline_keyboard", keyboard)
        }
    }
    
    fun isValidToken(): Boolean {
        return token.matches(Regex("\\d+:[A-Za-z0-9_-]{35}"))
    }
}
