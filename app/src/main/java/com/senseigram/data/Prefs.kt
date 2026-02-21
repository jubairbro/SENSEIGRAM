package com.senseigram.data

import com.senseigram.App
import org.json.JSONArray
import org.json.JSONObject

object Prefs {
    var token: String
        get() = App.prefs().getString("token", "") ?: ""
        set(v) = App.prefs().edit().putString("token", v).apply()
    
    var botName: String
        get() = App.prefs().getString("botName", "") ?: ""
        set(v) = App.prefs().edit().putString("botName", v).apply()
    
    var botUsername: String
        get() = App.prefs().getString("botUser", "") ?: ""
        set(v) = App.prefs().edit().putString("botUser", v).apply()
    
    var seenSub: Boolean
        get() = App.prefs().getBoolean("seenSub", false)
        set(v) = App.prefs().edit().putBoolean("seenSub", v).apply()
    
    var theme: Int
        get() = App.prefs().getInt("theme", 0)
        set(v) = App.prefs().edit().putInt("theme", v).apply()
    
    var accent: String
        get() = App.prefs().getString("accent", "emerald") ?: "emerald"
        set(v) = App.prefs().edit().putString("accent", v).apply()
    
    fun getChats(): List<SavedChat> {
        val json = App.prefs().getString("chats", "[]") ?: "[]"
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SavedChat(o.getLong("id"), o.getString("title"), o.getString("type"), o.optLong("time", System.currentTimeMillis()))
            }.sortedByDescending { it.time }
        } catch (e: Exception) { emptyList() }
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
        App.prefs().edit().putString("chats", arr.toString()).apply()
    }
}
