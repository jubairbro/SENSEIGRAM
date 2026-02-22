package com.senseigram.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.senseigram.R
import com.senseigram.data.AccentColors
import com.senseigram.data.Prefs
import com.senseigram.databinding.ActivityMainBinding
import com.senseigram.ui.compose.ComposeFragment
import com.senseigram.ui.home.HomeFragment
import com.senseigram.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var prefs: Prefs
    private var currentFragment: Fragment? = null

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

        setupToolbar()
        setupNavigation()
        setupBottomNav()

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = getString(R.string.app_name)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        
        updateToolbarColors()
    }

    private fun updateToolbarColors() {
        val accentColor = AccentColors.getPrimary(prefs.accent)
        binding.toolbar.setTitleTextColor(accentColor)
    }

    private fun setupNavigation() {
        updateNavHeader()
        
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    showFragment(HomeFragment())
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_compose -> {
                    showFragment(ComposeFragment())
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_disconnect -> {
                    logout()
                    true
                }
                else -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    private fun setupBottomNav() {
        binding.navHomeBtn.setOnClickListener {
            showFragment(HomeFragment())
            updateBottomNavSelection(0)
        }
        
        binding.navComposeBtn.setOnClickListener {
            showFragment(ComposeFragment())
            updateBottomNavSelection(1)
        }
        
        binding.fabCompose.setOnClickListener {
            showFragment(ComposeFragment())
            updateBottomNavSelection(1)
        }
    }

    private fun updateBottomNavSelection(index: Int) {
        val homeIcon = binding.navHomeBtn.getChildAt(0) as? android.widget.ImageView
        val homeText = binding.navHomeBtn.getChildAt(1) as? android.widget.TextView
        val composeIcon = binding.navComposeBtn.getChildAt(0) as? android.widget.ImageView
        val composeText = binding.navComposeBtn.getChildAt(1) as? android.widget.TextView
        
        val accentColor = AccentColors.getPrimary(prefs.accent)
        val defaultColor = resources.getColor(R.color.text_secondary_light, null)
        
        if (index == 0) {
            homeIcon?.setColorFilter(accentColor)
            homeText?.setTextColor(accentColor)
            composeIcon?.setColorFilter(defaultColor)
            composeText?.setTextColor(defaultColor)
        } else {
            composeIcon?.setColorFilter(accentColor)
            composeText?.setTextColor(accentColor)
            homeIcon?.setColorFilter(defaultColor)
            homeText?.setTextColor(defaultColor)
        }
    }

    fun showFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        
        // Update toolbar title
        val title = when (fragment) {
            is HomeFragment -> getString(R.string.app_name)
            is ComposeFragment -> getString(R.string.nav_compose)
            else -> getString(R.string.app_name)
        }
        supportActionBar?.title = title
    }

    fun switchToCompose() {
        showFragment(ComposeFragment())
        updateBottomNavSelection(1)
    }

    fun updateNavHeader() {
        val headerView = binding.navigationView.getHeaderView(0)
        val botName = headerView.findViewById<android.widget.TextView>(R.id.navBotName)
        val botUsername = headerView.findViewById<android.widget.TextView>(R.id.navBotUsername)
        
        val name = prefs.botName.ifEmpty { getString(R.string.app_name) }
        val username = prefs.botUsername
        
        botName?.text = name
        botUsername?.text = if (username.isNotEmpty()) "@$username" else getString(R.string.connect_your_bot)
    }

    fun applyTheme() {
        val themeIndex = prefs.theme
        
        when (themeIndex) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        when (themeIndex) {
            2 -> setTheme(R.style.Theme_SenseiGram_Amoled)
            else -> setTheme(R.style.Theme_SenseiGram)
        }
    }

    fun logout() {
        prefs.token = ""
        prefs.botName = ""
        prefs.botUsername = ""
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (currentFragment !is HomeFragment) {
            showFragment(HomeFragment())
            updateBottomNavSelection(0)
        } else {
            super.onBackPressed()
        }
    }
}
