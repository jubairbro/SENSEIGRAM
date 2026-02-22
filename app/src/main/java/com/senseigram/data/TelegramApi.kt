package com.senseigram.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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
                Bot(r.getLong("id"), r.getString("first_name"), r.optString("username").ifEmpty { null })
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun getChat(token: String, chatId: String): Chat? = withContext(Dispatchers.IO) {
        try {
            val body = FormBody.Builder().add("chat_id", chatId).build()
            val res = call(token, "getChat", body)
            if (res.optBoolean("ok")) {
                val r = res.getJSONObject("result")
                Chat(
                    r.getLong("id"),
                    r.optString("title").ifEmpty { null },
                    r.optString("username").ifEmpty { null },
                    r.getString("type")
                )
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun sendMessage(
        token: String, chatId: String, text: String,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean, disablePreview: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                put("parse_mode", "HTML")
                if (silent) put("disable_notification", true)
                if (protect) put("protect_content", true)
                if (disablePreview) put("disable_web_page_preview", true)
                if (!buttons.isNullOrEmpty()) put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "sendMessage", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendPhoto(
        token: String, chatId: String, file: File, caption: String?,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean, spoiler: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("photo", file.name, file.asRequestBody("image/*".toMediaType()))
                .apply { caption?.let { addFormDataPart("caption", it); addFormDataPart("parse_mode", "HTML") } }
                .apply { if (silent) addFormDataPart("disable_notification", "true") }
                .apply { if (protect) addFormDataPart("protect_content", "true") }
                .apply { if (spoiler) addFormDataPart("has_spoiler", "true") }
                .apply { if (!buttons.isNullOrEmpty()) addFormDataPart("reply_markup", buildKeyboard(buttons).toString()) }
                .build()
            val res = call(token, "sendPhoto", body)
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendPhotoByUrl(
        token: String, chatId: String, url: String, caption: String?,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean, spoiler: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("photo", url)
                caption?.let { put("caption", it); put("parse_mode", "HTML") }
                if (silent) put("disable_notification", true)
                if (protect) put("protect_content", true)
                if (spoiler) put("has_spoiler", true)
                if (!buttons.isNullOrEmpty()) put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "sendPhoto", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendVideo(
        token: String, chatId: String, file: File, caption: String?,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean, spoiler: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("video", file.name, file.asRequestBody("video/*".toMediaType()))
                .apply { caption?.let { addFormDataPart("caption", it); addFormDataPart("parse_mode", "HTML") } }
                .apply { if (silent) addFormDataPart("disable_notification", "true") }
                .apply { if (protect) addFormDataPart("protect_content", "true") }
                .apply { if (spoiler) addFormDataPart("has_spoiler", "true") }
                .apply { if (!buttons.isNullOrEmpty()) addFormDataPart("reply_markup", buildKeyboard(buttons).toString()) }
                .build()
            val res = call(token, "sendVideo", body)
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendVideoByUrl(
        token: String, chatId: String, url: String, caption: String?,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean, spoiler: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("video", url)
                caption?.let { put("caption", it); put("parse_mode", "HTML") }
                if (silent) put("disable_notification", true)
                if (protect) put("protect_content", true)
                if (spoiler) put("has_spoiler", true)
                if (!buttons.isNullOrEmpty()) put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "sendVideo", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendDocument(
        token: String, chatId: String, file: File, caption: String?,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("document", file.name, file.asRequestBody("*/*".toMediaType()))
                .apply { caption?.let { addFormDataPart("caption", it); addFormDataPart("parse_mode", "HTML") } }
                .apply { if (silent) addFormDataPart("disable_notification", "true") }
                .apply { if (protect) addFormDataPart("protect_content", "true") }
                .apply { if (!buttons.isNullOrEmpty()) addFormDataPart("reply_markup", buildKeyboard(buttons).toString()) }
                .build()
            val res = call(token, "sendDocument", body)
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendDocumentByUrl(
        token: String, chatId: String, url: String, caption: String?,
        buttons: List<List<InlineBtn>>?, silent: Boolean, protect: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("document", url)
                caption?.let { put("caption", it); put("parse_mode", "HTML") }
                if (silent) put("disable_notification", true)
                if (protect) put("protect_content", true)
                if (!buttons.isNullOrEmpty()) put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "sendDocument", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun editMessageText(
        token: String, chatId: String, msgId: Long, text: String, buttons: List<List<InlineBtn>>?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("message_id", msgId)
                put("text", text)
                put("parse_mode", "HTML")
                if (buttons != null) put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "editMessageText", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun editMessageCaption(
        token: String, chatId: String, msgId: Long, caption: String, buttons: List<List<InlineBtn>>?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("message_id", msgId)
                put("caption", caption)
                put("parse_mode", "HTML")
                if (buttons != null) put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "editMessageCaption", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun editMessageReplyMarkup(
        token: String, chatId: String, msgId: Long, buttons: List<List<InlineBtn>>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", chatId)
                put("message_id", msgId)
                put("reply_markup", buildKeyboard(buttons))
            }
            val res = call(token, "editMessageReplyMarkup", json.toString().toRequestBody("application/json".toMediaType()))
            if (res.optBoolean("ok")) Result.success(true)
            else Result.failure(Exception(res.optString("description", "Unknown error")))
        } catch (e: Exception) { Result.failure(e) }
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
            .apply {
                if (body != null) post(body)
                else post("{}".toRequestBody("application/json".toMediaType()))
            }
            .build()
        return JSONObject(client.newCall(req).execute().body?.string() ?: "{}")
    }
}

object AccentColors {
    val colors = listOf(
        Triple("Emerald", 0xFF10B981.toInt(), 0xFF059669.toInt()),
        Triple("Blue", 0xFF3B82F6.toInt(), 0xFF1D4ED8.toInt()),
        Triple("Violet", 0xFF8B5CF6.toInt(), 0xFF6D28D9.toInt()),
        Triple("Rose", 0xFFF43F5E.toInt(), 0xFFBE123C.toInt()),
        Triple("Amber", 0xFFF59E0B.toInt(), 0xFFB45309.toInt())
    )

    fun get(index: Int) = colors.getOrElse(index) { colors[0] }
    fun getPrimary(index: Int) = get(index).second
    fun getDark(index: Int) = get(index).third
}
