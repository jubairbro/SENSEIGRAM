package com.senseigram

import android.app.Application
import com.senseigram.data.Prefs

class SenseiGramApp : Application() {
    companion object {
        lateinit var prefs: Prefs
    }
    
    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
    }
}
