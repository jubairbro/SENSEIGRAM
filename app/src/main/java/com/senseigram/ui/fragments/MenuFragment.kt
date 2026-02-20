package com.senseigram.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.*
import com.senseigram.data.remote.TelegramService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {
    
    private val viewModel: MainViewModel by activityViewModels()
    
    private lateinit var etBotToken: TextInputEditText
    private lateinit var tvBotInfo: TextView
    private lateinit var btnValidate: Button
    private lateinit var btnDisconnect: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var etNewChatId: TextInputEditText
    private lateinit var etNewChatTitle: TextInputEditText
    private lateinit var btnAddChat: Button
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupThemeSelector(view)
        setupAccentSelector(view)
        observeData()
    }
    
    private fun initViews(view: View) {
        etBotToken = view.findViewById(R.id.etBotToken)
        tvBotInfo = view.findViewById(R.id.tvBotInfo)
        btnValidate = view.findViewById(R.id.btnValidate)
        btnDisconnect = view.findViewById(R.id.btnDisconnect)
        progressBar = view.findViewById(R.id.progressBar)
        
        etNewChatId = view.findViewById(R.id.etNewChatId)
        etNewChatTitle = view.findViewById(R.id.etNewChatTitle)
        btnAddChat = view.findViewById(R.id.btnAddChat)
        
        lifecycleScope.launch {
            val token = SenseiGramApp.preferenceManager.botToken.first()
            if (token.isNotEmpty()) {
                etBotToken.setText(token)
                validateBot(token)
            }
        }
        
        btnValidate.setOnClickListener {
            val token = etBotToken.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter bot token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            validateBot(token)
        }
        
        btnDisconnect.setOnClickListener {
            lifecycleScope.launch {
                SenseiGramApp.preferenceManager.setBotToken("")
                tvBotInfo.text = "Not connected"
                btnDisconnect.visibility = View.GONE
                Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnAddChat.setOnClickListener {
            addChat()
        }
    }
    
    private fun validateBot(token: String) {
        progressBar.visibility = View.VISIBLE
        btnValidate.isEnabled = false
        
        lifecycleScope.launch {
            val service = TelegramService(token)
            service.getMe().fold(
                onSuccess = { user ->
                    SenseiGramApp.preferenceManager.setBotToken(token)
                    tvBotInfo.text = "Connected: @${user.username ?: user.first_name}"
                    btnDisconnect.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    tvBotInfo.text = "Error: ${error.message}"
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            )
            
            progressBar.visibility = View.GONE
            btnValidate.isEnabled = true
        }
    }
    
    private fun addChat() {
        val chatId = etNewChatId.text.toString().trim()
        val title = etNewChatTitle.text.toString().trim()
        
        if (chatId.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Chat ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            val token = SenseiGramApp.preferenceManager.botToken.first()
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Please connect bot first", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val service = TelegramService(token)
            var targetId = chatId
            if (!chatId.startsWith("@") && !chatId.startsWith("-") && chatId.toLongOrNull() == null) {
                targetId = "@$chatId"
            }
            
            service.getChat(targetId).fold(
                onSuccess = { chat ->
                    val savedChat = SavedChat(
                        id = chat.id,
                        title = title.ifEmpty { chat.title ?: chat.username ?: "Unknown" },
                        type = chat.type
                    )
                    viewModel.addSavedChat(savedChat)
                    Toast.makeText(requireContext(), "Chat added!", Toast.LENGTH_SHORT).show()
                    etNewChatId.text?.clear()
                    etNewChatTitle.text?.clear()
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    private fun setupThemeSelector(view: View) {
        val rgTheme = view.findViewById<RadioGroup>(R.id.rgTheme)
        
        lifecycleScope.launch {
            val currentTheme = SenseiGramApp.preferenceManager.theme.first()
            when (currentTheme) {
                AppTheme.LIGHT -> rgTheme.check(R.id.rbLight)
                AppTheme.DARK -> rgTheme.check(R.id.rbDark)
                AppTheme.AMOLED -> rgTheme.check(R.id.rbAmoled)
                AppTheme.SYSTEM -> rgTheme.check(R.id.rbSystem)
            }
        }
        
        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.rbLight -> AppTheme.LIGHT
                R.id.rbDark -> AppTheme.DARK
                R.id.rbAmoled -> AppTheme.AMOLED
                else -> AppTheme.SYSTEM
            }
            
            viewModel.setTheme(theme)
            val mode = when (theme) {
                AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                AppTheme.AMOLED -> AppCompatDelegate.MODE_NIGHT_YES
                AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
    
    private fun setupAccentSelector(view: View) {
        val accents = listOf(
            ColorAccent.EMERALD to view.findViewById<Button>(R.id.btnAccentEmerald),
            ColorAccent.BLUE to view.findViewById<Button>(R.id.btnAccentBlue),
            ColorAccent.VIOLET to view.findViewById<Button>(R.id.btnAccentViolet),
            ColorAccent.ROSE to view.findViewById<Button>(R.id.btnAccentRose),
            ColorAccent.AMBER to view.findViewById<Button>(R.id.btnAccentAmber)
        )
        
        lifecycleScope.launch {
            val currentAccent = SenseiGramApp.preferenceManager.accent.first()
            updateAccentButtons(accents, currentAccent)
        }
        
        accents.forEach { (accent, button) ->
            button.setOnClickListener {
                viewModel.setAccent(accent)
                updateAccentButtons(accents, accent)
            }
        }
    }
    
    private fun updateAccentButtons(
        accents: List<Pair<ColorAccent, Button>>,
        currentAccent: ColorAccent
    ) {
        accents.forEach { (accent, button) ->
            val isSelected = accent == currentAccent
            button.alpha = if (isSelected) 1.0f else 0.5f
        }
    }
    
    private fun observeData() {
        viewModel.botUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvBotInfo.text = "Connected: @${user.username ?: user.first_name}"
                btnDisconnect.visibility = View.VISIBLE
            }
        }
    }
}
