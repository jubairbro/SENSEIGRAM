package com.senseigram.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.databinding.ActivityMainBinding
import com.senseigram.ui.compose.ComposeFragment
import com.senseigram.ui.home.HomeFragment
import com.senseigram.ui.login.LoginActivity
import com.senseigram.ui.menu.MenuFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        applyTheme()
        super.onCreate(savedInstanceState)

        if (prefs.token.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_compose -> ComposeFragment()
                R.id.nav_menu -> MenuFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }
    }

    fun switchToTab(tabId: Int) {
        binding.bottomNav.selectedItemId = tabId
    }

    fun applyTheme() {
        when (prefs.theme) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                setTheme(R.style.Theme_SenseiGram)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.Theme_SenseiGram_Dark)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                setTheme(R.style.Theme_SenseiGram_Amoled)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                setTheme(R.style.Theme_SenseiGram)
            }
        }
    }

    fun logout() {
        prefs.token = ""
        prefs.botName = ""
        prefs.botUsername = ""
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
