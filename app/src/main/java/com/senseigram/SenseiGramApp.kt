package com.senseigram

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.senseigram.data.local.PreferenceManager
import com.senseigram.data.model.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SenseiGramApp : Application() {
    
    companion object {
        lateinit var instance: SenseiGramApp
            private set
        lateinit var preferenceManager: PreferenceManager
            private set
    }
    
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
        
        applicationScope.launch {
            applyTheme()
        }
        
        createNotificationChannels()
    }
    
    private suspend fun applyTheme() {
        val theme = preferenceManager.theme.first()
        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.AMOLED -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "senseigram_channel"
            val channelName = "SenseiGram Notifications"
            val channelDescription = "General notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
