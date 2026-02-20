package com.senseigram.data

import android.content.Context

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("senseigram", Context.MODE_PRIVATE)
    
    var botToken: String
        get() = prefs.getString("token", "") ?: ""
        set(value) = prefs.edit().putString("token", value).apply()
    
    var botName: String
        get() = prefs.getString("name", "") ?: ""
        set(value) = prefs.edit().putString("name", value).apply()
    
    var botUsername: String
        get() = prefs.getString("username", "") ?: ""
        set(value) = prefs.edit().putString("username", value).apply()
    
    var seenSub: Boolean
        get() = prefs.getBoolean("seen_sub", false)
        set(value) = prefs.edit().putBoolean("seen_sub", value).apply()
}
