package com.senseigram

import android.app.Application
import com.senseigram.data.local.PreferenceManager

class SenseiGramApp : Application() {
    
    companion object {
        lateinit var prefs: PreferenceManager
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager(this)
    }
}
