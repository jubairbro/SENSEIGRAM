package com.senseigram.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.AppTheme
import com.senseigram.data.model.ColorAccent
import com.senseigram.data.model.TelegramUser
import com.senseigram.data.remote.TelegramService
import com.senseigram.ui.fragments.ComposeFragment
import com.senseigram.ui.fragments.HomeFragment
import com.senseigram.ui.fragments.MenuFragment
import com.senseigram.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var currentFragment: Fragment? = null
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupNavigation()
        
        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance())
        }
        
        observeData()
    }
    
    private fun setupNavigation() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            SenseiGramApp.preferenceManager.botToken.collect { token ->
                if (token.isNotEmpty()) {
                    loadBotInfo(token)
                }
            }
        }
        
        lifecycleScope.launch {
            SenseiGramApp.preferenceManager.accent.collect { accent ->
                updateAccentColor(accent)
            }
        }
    }
    
    private fun loadBotInfo(token: String) {
        lifecycleScope.launch {
            val service = TelegramService(token)
            service.getMe().fold(
                onSuccess = { user ->
                    updateNavHeader(user)
                },
                onFailure = {
                    updateNavHeader(null)
                }
            )
        }
    }
    
    private fun updateNavHeader(user: TelegramUser?) {
        val headerView = navigationView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvBotName)
        val tvUsername = headerView.findViewById<TextView>(R.id.tvBotUsername)
        val ivAvatar = headerView.findViewById<ImageView>(R.id.ivBotAvatar)
        
        if (user != null) {
            tvName.text = user.first_name
            tvUsername.text = "@${user.username ?: "bot"}"
            ivAvatar.visibility = View.VISIBLE
        } else {
            tvName.text = "SenseiGram"
            tvUsername.text = "Telegram Bot Manager"
        }
    }
    
    private fun updateAccentColor(accent: ColorAccent) {
        viewModel.setAccent(accent)
    }
    
    private fun loadFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                loadFragment(HomeFragment.newInstance())
            }
            R.id.nav_compose -> {
                loadFragment(ComposeFragment.newInstance())
            }
            R.id.nav_menu -> {
                loadFragment(MenuFragment.newInstance())
            }
            R.id.nav_saved_chats -> {
                startActivity(Intent(this, SavedChatsActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_channel -> {
                openChannel()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun openChannel() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+5ygHfkZxVBc0Mjdl"))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
