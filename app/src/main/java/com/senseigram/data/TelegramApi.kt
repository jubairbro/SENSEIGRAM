package com.senseigram.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object TelegramApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    
    suspend fun getMe(token: String): Bot? = withContext(Dispatchers.IO) {
        try {
            val res = call(token, "getMe")
            if (res.optBoolean("ok")) {
                val r = res.getJSONObject("result")
                Bot(r.getLong("id"), r.getString("first_name"), r.optString("username"))
            } else null
        } catch (e: Exception) { null }
    }
    
    suspend fun getChat(token: String, chatId: String): Chat? = withContext(Dispatchers.IO) {
        try {
            val body = FormBody.Builder().add("chat_id", chatId).build()
            val res = call(token, "getChat", body)
            if (res.optBoolean("ok")) {
                val r = res.getJSONObject("result")
                Chat(r.getLong("id"), r.optString("title"), r.optString("username"), r.getString("type"))
            } else null
        } catch (e: Exception) { null }
    }
    
    suspend fun sendMessage(
        token: String,
        chatId: String,
        text: String,
        buttons: List<List<InlineBtn>>? = null,
        parseMode: String = "HTML"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                put("parse_mode", parseMode)
                buttons?.let { put("reply_markup", buildKeyboard(it)) }
            }
            val res = call(token, "sendMessage", json.toString().toRequestBody("application/json".toMediaType()))
            res.optBoolean("ok")
        } catch (e: Exception) { false }
    }
    
    suspend fun sendPhoto(token: String, chatId: String, file: File, caption: String?, buttons: List<List<InlineBtn>>?): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("photo", file.name, file.asRequestBody("image/*".toMediaType()))
                .apply { caption?.let { addFormDataPart("caption", it); addFormDataPart("parse_mode", "HTML") } }
                .apply { buttons?.let { addFormDataPart("reply_markup", buildKeyboard(it).toString()) } }
                .build()
            call(token, "sendPhoto", body).optBoolean("ok")
        } catch (e: Exception) { false }
    }
    
    suspend fun sendVideo(token: String, chatId: String, file: File, caption: String?, buttons: List<List<InlineBtn>>?): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("video", file.name, file.asRequestBody("video/*".toMediaType()))
                .apply { caption?.let { addFormDataPart("caption", it); addFormDataPart("parse_mode", "HTML") } }
                .apply { buttons?.let { addFormDataPart("reply_markup", buildKeyboard(it).toString()) } }
                .build()
            call(token, "sendVideo", body).optBoolean("ok")
        } catch (e: Exception) { false }
    }
    
    suspend fun sendDocument(token: String, chatId: String, file: File, caption: String?, buttons: List<List<InlineBtn>>?): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("document", file.name, file.asRequestBody("*/*".toMediaType()))
                .apply { caption?.let { addFormDataPart("caption", it); addFormDataPart("parse_mode", "HTML") } }
                .apply { buttons?.let { addFormDataPart("reply_markup", buildKeyboard(it).toString()) } }
                .build()
            call(token, "sendDocument", body).optBoolean("ok")
        } catch (e: Exception) { false }
    }
    
    private fun buildKeyboard(buttons: List<List<InlineBtn>>): JSONObject {
        val keyboard = JSONArray()
        buttons.forEach { row ->
            val rowArr = JSONArray()
            row.forEach { btn ->
                rowArr.put(JSONObject().apply {
                    put("text", btn.text)
                    btn.url?.let { put("url", it) }
                    btn.callback?.let { put("callback_data", it) }
                })
            }
            keyboard.put(rowArr)
        }
        return JSONObject().put("inline_keyboard", keyboard)
    }
    
    private fun call(token: String, method: String, body: RequestBody? = null): JSONObject {
        val req = Request.Builder()
            .url("https://api.telegram.org/bot$token/$method")
            .apply { if (body != null) post(body) else post("{}".toRequestBody("application/json".toMediaType())) }
            .build()
        return JSONObject(client.newCall(req).execute().body?.string() ?: "{}")
    }
}
