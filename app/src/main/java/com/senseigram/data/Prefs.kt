package com.senseigram.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class Prefs(private val ctx: Context) {
    private val p: SharedPreferences = ctx.getSharedPreferences("senseigram", Context.MODE_PRIVATE)

    var token: String
        get() = p.getString("token", "") ?: ""
        set(v) = p.edit().putString("token", v).apply()

    var botName: String
        get() = p.getString("botName", "") ?: ""
        set(v) = p.edit().putString("botName", v).apply()

    var botUsername: String
        get() = p.getString("botUser", "") ?: ""
        set(v) = p.edit().putString("botUser", v).apply()

    var seenTut: Boolean
        get() = p.getBoolean("seenTut", false)
        set(v) = p.edit().putBoolean("seenTut", v).apply()

    var theme: Int
        get() = p.getInt("theme", 3)
        set(v) = p.edit().putInt("theme", v).apply()

    var accent: Int
        get() = p.getInt("accent", 0)
        set(v) = p.edit().putInt("accent", v).apply()

    var userName: String
        get() = p.getString("userName", "") ?: ""
        set(v) = p.edit().putString("userName", v).apply()

    var smoothScroll: Boolean
        get() = p.getBoolean("smoothScroll", true)
        set(v) = p.edit().putBoolean("smoothScroll", v).apply()

    var hapticFeedback: Boolean
        get() = p.getBoolean("hapticFeedback", true)
        set(v) = p.edit().putBoolean("hapticFeedback", v).apply()

    fun getChats(): List<SavedChat> {
        val json = p.getString("chats", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavedChat(
                    o.getLong("id"),
                    o.getString("title"),
                    o.getString("type"),
                    o.optLong("time", System.currentTimeMillis())
                )
            }.sortedByDescending { it.time }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addChat(chat: SavedChat) {
        val chats = getChats().filter { it.id != chat.id }.toMutableList()
        chats.add(0, chat)
        saveChats(chats)
    }

    fun removeChat(id: Long) = saveChats(getChats().filter { it.id != id })

    private fun saveChats(chats: List<SavedChat>) {
        val arr = JSONArray()
        chats.forEach { c ->
            arr.put(JSONObject().apply {
                put("id", c.id)
                put("title", c.title)
                put("type", c.type)
                put("time", c.time)
            })
        }
        p.edit().putString("chats", arr.toString()).apply()
    }

    fun getDrafts(): List<MessageDraft> {
        val json = p.getString("drafts", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                MessageDraft(
                    o.getString("id"),
                    o.getString("chatId"),
                    o.getString("text"),
                    parseButtons(o.optJSONArray("buttons")),
                    o.optLong("timestamp", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addDraft(draft: MessageDraft) {
        val drafts = getDrafts().toMutableList()
        drafts.add(0, draft)
        val arr = JSONArray()
        drafts.take(10).forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("chatId", d.chatId)
                put("text", d.text)
                put("buttons", encodeButtons(d.buttons))
                put("timestamp", d.timestamp)
            })
        }
        p.edit().putString("drafts", arr.toString()).apply()
    }

    fun removeDraft(id: String) {
        val drafts = getDrafts().filter { it.id != id }
        val arr = JSONArray()
        drafts.forEach { d ->
            arr.put(JSONObject().apply {
                put("id", d.id)
                put("chatId", d.chatId)
                put("text", d.text)
                put("buttons", encodeButtons(d.buttons))
                put("timestamp", d.timestamp)
            })
        }
        p.edit().putString("drafts", arr.toString()).apply()
    }

    private fun parseButtons(arr: JSONArray?): List<List<InlineBtn>> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { i ->
            val row = arr.getJSONArray(i)
            (0 until row.length()).map { j ->
                val b = row.getJSONObject(j)
                InlineBtn(
                    b.getString("text"),
                    b.optString("url").ifEmpty { null },
                    b.optString("callback").ifEmpty { null },
                    b.optInt("style", 0)
                )
            }
        }
    }

    private fun encodeButtons(buttons: List<List<InlineBtn>>): JSONArray {
        val arr = JSONArray()
        buttons.forEach { row ->
            val rowArr = JSONArray()
            row.forEach { b ->
                rowArr.put(JSONObject().apply {
                    put("text", b.text)
                    b.url?.let { put("url", it) }
                    b.callback?.let { put("callback", it) }
                    put("style", b.style)
                })
            }
            arr.put(rowArr)
        }
        return arr
    }
}

data class Bot(val id: Long, val name: String, val username: String?)
data class Chat(
    val id: Long, 
    val title: String?, 
    val username: String?, 
    val type: String, 
    val firstName: String? = null, 
    val lastName: String? = null,
    val description: String? = null,
    val membersCount: Int? = null
)
data class ChatLookupResult(val chat: Chat?, val error: String?)
data class SavedChat(val id: Long, val title: String, val type: String, val time: Long)
data class InlineBtn(val text: String, val url: String?, val callback: String?, val style: Int = 0)
data class MessageDraft(
    val id: String,
    val chatId: String,
    val text: String,
    val buttons: List<List<InlineBtn>>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MediaType { TEXT, PHOTO, VIDEO, DOCUMENT }
