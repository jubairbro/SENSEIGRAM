package com.senseigram.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.senseigram.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "senseigram_prefs")

class PreferenceManager(private val context: Context) {
    
    companion object {
        private val KEY_BOT_TOKEN = stringPreferencesKey("bot_token")
        private val KEY_THEME = intPreferencesKey("theme")
        private val KEY_ACCENT = stringPreferencesKey("accent")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_SAVED_CHATS = stringPreferencesKey("saved_chats")
        private val KEY_DRAFTS = stringPreferencesKey("drafts")
        private val KEY_HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
        private val KEY_HAS_SEEN_SUBSCRIPTION = booleanPreferencesKey("has_seen_subscription")
        private val gson = Gson()
    }
    
    val botToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BOT_TOKEN] ?: ""
    }
    
    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME] ?: 3) {
            0 -> AppTheme.LIGHT
            1 -> AppTheme.DARK
            2 -> AppTheme.AMOLED
            else -> AppTheme.SYSTEM
        }
    }
    
    val accent: Flow<ColorAccent> = context.dataStore.data.map { prefs ->
        try {
            ColorAccent.valueOf(prefs[KEY_ACCENT] ?: "EMERALD")
        } catch (e: Exception) {
            ColorAccent.EMERALD
        }
    }
    
    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }
    
    val savedChats: Flow<List<SavedChat>> = context.dataStore.data.map { prefs ->
        try {
            val json = prefs[KEY_SAVED_CHATS] ?: "[]"
            val type = object : TypeToken<List<SavedChat>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    val drafts: Flow<List<MessageDraft>> = context.dataStore.data.map { prefs ->
        try {
            val json = prefs[KEY_DRAFTS] ?: "[]"
            val type = object : TypeToken<List<MessageDraft>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    val hasSeenTutorial: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAS_SEEN_TUTORIAL] ?: false
    }
    
    val hasSeenSubscription: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAS_SEEN_SUBSCRIPTION] ?: false
    }
    
    suspend fun setBotToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BOT_TOKEN] = token
        }
    }
    
    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = when (theme) {
                AppTheme.LIGHT -> 0
                AppTheme.DARK -> 1
                AppTheme.AMOLED -> 2
                AppTheme.SYSTEM -> 3
            }
        }
    }
    
    suspend fun setAccent(accent: ColorAccent) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCENT] = accent.name
        }
    }
    
    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
        }
    }
    
    suspend fun setSavedChats(chats: List<SavedChat>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CHATS] = gson.toJson(chats)
        }
    }
    
    suspend fun addSavedChat(chat: SavedChat) {
        val current = savedChats.first().toMutableList()
        if (current.none { it.id == chat.id }) {
            current.add(0, chat)
            setSavedChats(current)
        }
    }
    
    suspend fun removeSavedChat(chatId: Long) {
        val current = savedChats.first().toMutableList()
        current.removeAll { it.id == chatId }
        setSavedChats(current)
    }
    
    suspend fun setDrafts(drafts: List<MessageDraft>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DRAFTS] = gson.toJson(drafts.take(10))
        }
    }
    
    suspend fun addDraft(draft: MessageDraft) {
        val current = drafts.first().toMutableList()
        current.add(0, draft)
        setDrafts(current.take(10))
    }
    
    suspend fun removeDraft(draftId: String) {
        val current = drafts.first().toMutableList()
        current.removeAll { it.id == draftId }
        setDrafts(current)
    }
    
    suspend fun setHasSeenTutorial(seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_SEEN_TUTORIAL] = seen
        }
    }
    
    suspend fun setHasSeenSubscription(seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_SEEN_SUBSCRIPTION] = seen
        }
    }
    
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
