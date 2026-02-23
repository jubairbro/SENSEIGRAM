package com.senseigram.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.senseigram.R
import com.senseigram.data.AccentColors
import com.senseigram.data.Prefs
import com.senseigram.data.SavedChat
import com.senseigram.data.TelegramApi
import com.senseigram.databinding.ActivityMainBinding
import com.senseigram.databinding.BottomSheetAccentBinding
import com.senseigram.databinding.BottomSheetBotBinding
import com.senseigram.databinding.BottomSheetChannelsBinding
import com.senseigram.databinding.BottomSheetLookupBinding
import com.senseigram.databinding.BottomSheetThemeBinding
import com.senseigram.ui.adapters.MenuChatAdapter
import com.senseigram.ui.compose.ComposeFragment
import com.senseigram.ui.home.HomeFragment
import com.senseigram.ui.login.LoginActivity
import kotlinx.coroutines.launch

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
                R.id.nav_theme -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    showThemeBottomSheet()
                    true
                }
                R.id.nav_accent -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    showAccentBottomSheet()
                    true
                }
                R.id.nav_bot -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    showBotBottomSheet()
                    true
                }
                R.id.nav_lookup -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    showLookupBottomSheet()
                    true
                }
                R.id.nav_channels -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    showChannelsBottomSheet()
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

    // ─── Bottom Sheets ─────────────────────────────────────────────

    private fun showThemeBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetThemeBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        
        updateThemeHighlight(sheetBinding)
        
        sheetBinding.themeLight.setOnClickListener { setThemeOption(0, sheetBinding) }
        sheetBinding.themeDark.setOnClickListener { setThemeOption(1, sheetBinding) }
        sheetBinding.themeAmoled.setOnClickListener { setThemeOption(2, sheetBinding) }
        sheetBinding.themeSystem.setOnClickListener { setThemeOption(3, sheetBinding) }
        
        dialog.show()
    }

    private fun setThemeOption(themeIndex: Int, sheetBinding: BottomSheetThemeBinding) {
        prefs.theme = themeIndex
        updateThemeHighlight(sheetBinding)
        applyTheme()
        recreate()
    }

    private fun updateThemeHighlight(sheetBinding: BottomSheetThemeBinding) {
        val currentTheme = prefs.theme
        val accentColor = AccentColors.getPrimary(prefs.accent)
        val defaultColor = resources.getColor(R.color.text_tertiary_light, null)
        val defaultTextColor = resources.getColor(R.color.text_secondary_light, null)

        sheetBinding.themeLightIcon.setColorFilter(if (currentTheme == 0) accentColor else defaultColor)
        sheetBinding.themeDarkIcon.setColorFilter(if (currentTheme == 1) accentColor else defaultColor)
        sheetBinding.themeAmoledIcon.setColorFilter(if (currentTheme == 2) accentColor else defaultColor)
        sheetBinding.themeSystemIcon.setColorFilter(if (currentTheme == 3) accentColor else defaultColor)

        sheetBinding.themeLightLabel.setTextColor(if (currentTheme == 0) accentColor else defaultTextColor)
        sheetBinding.themeDarkLabel.setTextColor(if (currentTheme == 1) accentColor else defaultTextColor)
        sheetBinding.themeAmoledLabel.setTextColor(if (currentTheme == 2) accentColor else defaultTextColor)
        sheetBinding.themeSystemLabel.setTextColor(if (currentTheme == 3) accentColor else defaultTextColor)
    }

    private fun showAccentBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetAccentBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        
        updateAccentHighlight(sheetBinding)
        
        sheetBinding.accentEmerald.setOnClickListener { setAccentOption(0, sheetBinding, dialog) }
        sheetBinding.accentBlue.setOnClickListener { setAccentOption(1, sheetBinding, dialog) }
        sheetBinding.accentViolet.setOnClickListener { setAccentOption(2, sheetBinding, dialog) }
        sheetBinding.accentRose.setOnClickListener { setAccentOption(3, sheetBinding, dialog) }
        sheetBinding.accentAmber.setOnClickListener { setAccentOption(4, sheetBinding, dialog) }
        
        dialog.show()
    }

    private fun setAccentOption(index: Int, sheetBinding: BottomSheetAccentBinding, dialog: BottomSheetDialog) {
        prefs.accent = index
        updateAccentHighlight(sheetBinding)
        updateToolbarColors()
        dialog.dismiss()
        recreate()
    }

    private fun updateAccentHighlight(sheetBinding: BottomSheetAccentBinding) {
        val current = prefs.accent
        val buttons = listOf(
            sheetBinding.accentEmerald,
            sheetBinding.accentBlue,
            sheetBinding.accentViolet,
            sheetBinding.accentRose,
            sheetBinding.accentAmber
        )
        buttons.forEachIndexed { i, btn ->
            btn.alpha = if (i == current) 1.0f else 0.5f
            btn.scaleX = if (i == current) 1.1f else 1.0f
            btn.scaleY = if (i == current) 1.1f else 1.0f
        }
    }

    private fun showBotBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetBotBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        
        sheetBinding.tokenInput.setText(prefs.token)
        
        val name = prefs.botName
        val username = prefs.botUsername
        if (name.isNotEmpty() || username.isNotEmpty()) {
            sheetBinding.connectedStatus.visibility = View.VISIBLE
            sheetBinding.connectedText.text = if (username.isNotEmpty()) {
                "${name.ifEmpty { "Bot" }} (@$username)"
            } else {
                name.ifEmpty { "Connected" }
            }
        } else {
            sheetBinding.connectedStatus.visibility = View.GONE
        }
        
        sheetBinding.validateBtn.setOnClickListener {
            val token = sheetBinding.tokenInput.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(this, R.string.error_invalid_token, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sheetBinding.validateBtn.isEnabled = false
            sheetBinding.validateBtn.text = getString(R.string.validating)
            
            lifecycleScope.launch {
                val bot = TelegramApi.getMe(token)
                if (bot != null) {
                    prefs.token = token
                    prefs.botName = bot.name
                    bot.username?.let { prefs.botUsername = it }
                    updateNavHeader()
                    sheetBinding.connectedStatus.visibility = View.VISIBLE
                    sheetBinding.connectedText.text = "${bot.name} (@${bot.username})"
                    Toast.makeText(this@MainActivity, "Connected!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, R.string.error_invalid_token, Toast.LENGTH_SHORT).show()
                }
                sheetBinding.validateBtn.isEnabled = true
                sheetBinding.validateBtn.text = getString(R.string.btn_connect)
            }
        }
        
        dialog.show()
    }

    private fun showLookupBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetLookupBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        
        sheetBinding.lookupBtn.setOnClickListener {
            val query = sheetBinding.lookupInput.text.toString().trim()
            if (query.isEmpty()) return@setOnClickListener
            
            val token = prefs.token
            if (token.isEmpty()) {
                Toast.makeText(this, R.string.error_bot_token_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sheetBinding.lookupBtn.isEnabled = false
            
            lifecycleScope.launch {
                val result = TelegramApi.getChat(token, query)
                if (result.chat != null) {
                    val chat = result.chat
                    sheetBinding.lookupResult.visibility = View.VISIBLE
                    
                    val displayName = when {
                        !chat.title.isNullOrEmpty() -> chat.title
                        !chat.firstName.isNullOrEmpty() -> {
                            chat.firstName + (chat.lastName?.let { " $it" } ?: "")
                        }
                        !chat.username.isNullOrEmpty() -> chat.username
                        else -> "Unknown"
                    }
                    sheetBinding.lookupTitle.text = displayName
                    sheetBinding.lookupType.text = chat.type.uppercase()
                    sheetBinding.lookupId.text = "ID: ${chat.id}"
                    
                    if (!chat.username.isNullOrEmpty()) {
                        sheetBinding.lookupUsername.visibility = View.VISIBLE
                        sheetBinding.lookupUsername.text = "@${chat.username}"
                    } else {
                        sheetBinding.lookupUsername.visibility = View.GONE
                    }
                    
                    // Members count
                    if (chat.membersCount != null) {
                        sheetBinding.lookupMembers.visibility = View.VISIBLE
                        sheetBinding.lookupMembers.text = "Members: ${String.format("%,d", chat.membersCount)}"
                    } else {
                        sheetBinding.lookupMembers.visibility = View.GONE
                    }
                    
                    // Description
                    if (!chat.description.isNullOrEmpty()) {
                        sheetBinding.lookupDescription.visibility = View.VISIBLE
                        sheetBinding.lookupDescription.text = chat.description
                    } else {
                        sheetBinding.lookupDescription.visibility = View.GONE
                    }
                } else {
                    sheetBinding.lookupResult.visibility = View.GONE
                    val errorMsg = result.error ?: getString(R.string.not_found, query)
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
                sheetBinding.lookupBtn.isEnabled = true
            }
        }
        
        dialog.show()
    }

    private fun showChannelsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetChannelsBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)
        
        val chatAdapter = MenuChatAdapter { chat ->
            prefs.removeChat(chat.id)
            refreshChannelsList(sheetBinding)
        }
        sheetBinding.channelsList.layoutManager = LinearLayoutManager(this)
        sheetBinding.channelsList.adapter = chatAdapter
        
        refreshChannelsList(sheetBinding)
        
        sheetBinding.addChannelBtn.setOnClickListener {
            val input = sheetBinding.newChannelInput.text.toString().trim()
            if (input.isEmpty()) return@setOnClickListener
            
            val token = prefs.token
            if (token.isEmpty()) {
                Toast.makeText(this, R.string.error_bot_token_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val existingChats = prefs.getChats()
            if (existingChats.any { it.id.toString() == input || it.title == input }) {
                Toast.makeText(this, R.string.chat_already_saved, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sheetBinding.addChannelBtn.isEnabled = false
            
            lifecycleScope.launch {
                val result = TelegramApi.getChat(token, input)
                if (result.chat != null) {
                    val chat = result.chat
                    val savedChat = SavedChat(
                        id = chat.id,
                        title = chat.title ?: chat.firstName ?: chat.username ?: input,
                        type = chat.type,
                        time = System.currentTimeMillis()
                    )
                    prefs.addChat(savedChat)
                    refreshChannelsList(sheetBinding)
                    sheetBinding.newChannelInput.text?.clear()
                    Toast.makeText(this@MainActivity, R.string.channel_added, Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = result.error ?: getString(R.string.not_found, input)
                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
                sheetBinding.addChannelBtn.isEnabled = true
            }
        }
        
        dialog.show()
    }

    private fun refreshChannelsList(sheetBinding: BottomSheetChannelsBinding) {
        val chats = prefs.getChats()
        (sheetBinding.channelsList.adapter as? MenuChatAdapter)?.submitList(chats)
        
        if (chats.isEmpty()) {
            sheetBinding.noChannelsText.visibility = View.VISIBLE
            sheetBinding.channelsList.visibility = View.GONE
        } else {
            sheetBinding.noChannelsText.visibility = View.GONE
            sheetBinding.channelsList.visibility = View.VISIBLE
        }
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
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.disconnect)
            .setMessage("Are you sure you want to disconnect?")
            .setPositiveButton(R.string.disconnect) { _, _ ->
                prefs.token = ""
                prefs.botName = ""
                prefs.botUsername = ""
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onBackPressed() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        if (currentFocus != null && imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)) {
            return
        }
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
