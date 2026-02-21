package com.senseigram

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        lateinit var ctx: Context
        fun prefs() = ctx.getSharedPreferences("bot", Context.MODE_PRIVATE)
    }
    override fun onCreate() {
        super.onCreate()
        ctx = this
    }
}
