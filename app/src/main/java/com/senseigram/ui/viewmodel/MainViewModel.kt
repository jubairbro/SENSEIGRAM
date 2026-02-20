package com.senseigram.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.*
import com.senseigram.data.remote.TelegramService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _botUser = MutableLiveData<TelegramUser?>()
    val botUser: LiveData<TelegramUser?> = _botUser
    
    private val _savedChats = MutableLiveData<List<SavedChat>>()
    val savedChats: LiveData<List<SavedChat>> = _savedChats
    
    private val _drafts = MutableLiveData<List<MessageDraft>>()
    val drafts: LiveData<List<MessageDraft>> = _drafts
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _accent = MutableLiveData<ColorAccent>()
    val accent: LiveData<ColorAccent> = _accent
    
    private val _theme = MutableLiveData<AppTheme>()
    val theme: LiveData<AppTheme> = _theme
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.savedChats.collect { chats ->
                _savedChats.postValue(chats)
            }
        }
        
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.drafts.collect { drafts ->
                _drafts.postValue(drafts)
            }
        }
        
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.accent.collect { accent ->
                _accent.postValue(accent)
            }
        }
        
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.theme.collect { theme ->
                _theme.postValue(theme)
            }
        }
        
        loadBotUser()
    }
    
    fun loadBotUser() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val token = SenseiGramApp.preferenceManager.botToken.first()
            if (token.isNotEmpty()) {
                val service = TelegramService(token)
                service.getMe().fold(
                    onSuccess = { user ->
                        _botUser.postValue(user)
                    },
                    onFailure = {
                        _error.postValue(it.message)
                        _botUser.postValue(null)
                    }
                )
            }
            _isLoading.postValue(false)
        }
    }
    
    fun setAccent(accent: ColorAccent) {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.setAccent(accent)
        }
    }
    
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.setTheme(theme)
        }
    }
    
    fun addSavedChat(chat: SavedChat) {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.addSavedChat(chat)
        }
    }
    
    fun removeSavedChat(chatId: Long) {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.removeSavedChat(chatId)
        }
    }
    
    fun addDraft(draft: MessageDraft) {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.addDraft(draft)
        }
    }
    
    fun removeDraft(draftId: String) {
        viewModelScope.launch {
            SenseiGramApp.preferenceManager.removeDraft(draftId)
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
