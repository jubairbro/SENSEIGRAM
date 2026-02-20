package com.senseigram.data.local

import android.content.Context
import com.senseigram.data.model.SavedChat

class PreferenceManager(context: Context) {
    
    private val prefs = context.getSharedPreferences("senseigram_prefs", Context.MODE_PRIVATE)
    
    fun getBotToken(): String = prefs.getString("bot_token", "") ?: ""
    
    fun setBotToken(token: String) {
        prefs.edit().putString("bot_token", token).apply()
    }
    
    fun getBotUser(): String = prefs.getString("bot_user", "") ?: ""
    
    fun setBotUser(user: String) {
        prefs.edit().putString("bot_user", user).apply()
    }
    
    fun getSavedChats(): List<SavedChat> {
        val json = prefs.getString("saved_chats", "[]") ?: "[]"
        return try {
            kotlinx.serialization.json.Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun addSavedChat(chat: SavedChat) {
        val chats = getSavedChats().toMutableList()
        if (chats.none { it.id == chat.id }) {
            chats.add(0, chat)
            val json = kotlinx.serialization.json.Json.encodeToString(chats)
            prefs.edit().putString("saved_chats", json).apply()
        }
    }
    
    fun removeSavedChat(chatId: Long) {
        val chats = getSavedChats().filter { it.id != chatId }
        val json = kotlinx.serialization.json.Json.encodeToString(chats)
        prefs.edit().putString("saved_chats", json).apply()
    }
    
    fun getTheme(): Int = prefs.getInt("theme", 0)
    
    fun setTheme(theme: Int) {
        prefs.edit().putInt("theme", theme).apply()
    }
    
    fun hasSeenSubscription(): Boolean = prefs.getBoolean("seen_subscription", false)
    
    fun setSeenSubscription(seen: Boolean) {
        prefs.edit().putBoolean("seen_subscription", seen).apply()
    }
}
