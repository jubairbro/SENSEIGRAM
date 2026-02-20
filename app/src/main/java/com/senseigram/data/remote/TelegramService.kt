package com.senseigram.data.remote

import com.senseigram.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class TelegramService(private val token: String) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun getMe(): Result<TelegramUser> = withContext(Dispatchers.IO) {
        try {
            val response = request("getMe")
            if (response.ok && response.result != null) {
                val userJson = Json.encodeToString(response.result)
                Result.success(Json.decodeFromString(userJson))
            } else {
                Result.failure(Exception(response.description ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getChat(chatId: String): Result<TelegramChat> = withContext(Dispatchers.IO) {
        try {
            val body = """{"chat_id":"$chatId"}""".toRequestBody(JSON_MEDIA)
            val response = request("getChat", body)
            if (response.ok && response.result != null) {
                val chatJson = Json.encodeToString(response.result)
                Result.success(Json.decodeFromString(chatJson))
            } else {
                Result.failure(Exception(response.description ?: "Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(
        chatId: String,
        text: String,
        parseMode: String = "HTML"
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val body = """{"chat_id":"$chatId","text":"${text.escapeJson()}","parse_mode":"$parseMode"}"""
                .toRequestBody(JSON_MEDIA)
            val response = request("sendMessage", body)
            if (response.ok && response.result != null) {
                val resultJson = Json.encodeToString(response.result)
                val result = Json.decodeFromString<SendMessageResult>(resultJson)
                Result.success(result.message_id)
            } else {
                Result.failure(Exception(response.description ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun request(method: String, body: okhttp3.RequestBody? = null): TelegramResponse<*> {
        val url = "$BASE_URL$token/$method"
        val requestBuilder = Request.Builder().url(url)
        
        if (body != null) {
            requestBuilder.post(body)
        } else {
            requestBuilder.post("{}".toRequestBody(JSON_MEDIA))
        }
        
        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string() ?: "{}"
        return json.decodeFromString<TelegramResponse<Any?>>(responseBody)
    }
    
    private fun String.escapeJson(): String {
        return this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    companion object {
        private const val BASE_URL = "https://api.telegram.org/bot"
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
