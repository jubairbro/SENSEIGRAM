package com.senseigram.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.AppTheme
import com.senseigram.data.model.ColorAccent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        setupThemeSettings()
        setupAccentSettings()
    }
    
    private fun setupThemeSettings() {
        lifecycleScope.launch {
            val currentTheme = SenseiGramApp.preferenceManager.theme.first()
            updateThemeSelection(currentTheme)
        }
    }
    
    private fun updateThemeSelection(theme: AppTheme) {
        val mode = when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.AMOLED -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    private fun setupAccentSettings() {
        lifecycleScope.launch {
            val currentAccent = SenseiGramApp.preferenceManager.accent.first()
            updateAccentSelection(currentAccent)
        }
    }
    
    private fun updateAccentSelection(accent: ColorAccent) {
        // Update UI based on accent
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
