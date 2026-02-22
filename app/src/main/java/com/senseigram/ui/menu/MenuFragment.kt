package com.senseigram.ui.menu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.senseigram.R
import com.senseigram.data.AccentColors
import com.senseigram.data.Prefs
import com.senseigram.data.SavedChat
import com.senseigram.data.TelegramApi
import com.senseigram.databinding.FragmentMenuBinding
import com.senseigram.ui.adapters.MenuChatAdapter
import com.senseigram.ui.main.MainActivity
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: Prefs
    private lateinit var chatAdapter: MenuChatAdapter
    private var isTokenVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        setupThemeSwitcher()
        setupBotConnection()
        setupLookup()
        setupTargets()
        setupAccentSelector()
        setupSocialLinks()
        setupJoinSupport()
        setupDisconnect()
    }

    override fun onResume() {
        super.onResume()
        refreshChannelsList()
        updateThemeHighlight()
        updateAccentHighlight()
        updateConnectionStatus()
    }

    // ─── Theme Switcher ─────────────────────────────────────────────

    private fun setupThemeSwitcher() {
        binding.themeLight.setOnClickListener { setTheme(0) }
        binding.themeDark.setOnClickListener { setTheme(1) }
        binding.themeAmoled.setOnClickListener { setTheme(2) }
        binding.themeSystem.setOnClickListener { setTheme(3) }
        updateThemeHighlight()
    }

    private fun setTheme(themeIndex: Int) {
        prefs.theme = themeIndex
        val activity = requireActivity() as? MainActivity ?: return
        activity.applyTheme()
        activity.recreate()
    }

    private fun updateThemeHighlight() {
        val currentTheme = prefs.theme
        val accentColor = AccentColors.getPrimary(prefs.accent)
        val defaultColor = resources.getColor(R.color.text_tertiary_light, null)
        val defaultTextColor = resources.getColor(R.color.text_secondary_light, null)

        binding.themeLightIcon.setColorFilter(if (currentTheme == 0) accentColor else defaultColor)
        binding.themeDarkIcon.setColorFilter(if (currentTheme == 1) accentColor else defaultColor)
        binding.themeAmoledIcon.setColorFilter(if (currentTheme == 2) accentColor else defaultColor)
        binding.themeSystemIcon.setColorFilter(if (currentTheme == 3) accentColor else defaultColor)

        binding.themeLightLabel.setTextColor(if (currentTheme == 0) accentColor else defaultTextColor)
        binding.themeDarkLabel.setTextColor(if (currentTheme == 1) accentColor else defaultTextColor)
        binding.themeAmoledLabel.setTextColor(if (currentTheme == 2) accentColor else defaultTextColor)
        binding.themeSystemLabel.setTextColor(if (currentTheme == 3) accentColor else defaultTextColor)
    }

    // ─── Bot Connection ─────────────────────────────────────────────

    private fun setupBotConnection() {
        binding.tokenInput.setText(prefs.token)

        binding.toggleTokenVisibility.setOnClickListener {
            isTokenVisible = !isTokenVisible
            if (isTokenVisible) {
                binding.tokenInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.toggleTokenVisibility.setImageResource(R.drawable.ic_eye)
            } else {
                binding.tokenInput.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.toggleTokenVisibility.setImageResource(R.drawable.ic_eye_off)
            }
            binding.tokenInput.setSelection(binding.tokenInput.text.length)
        }

        binding.validateBtn.setOnClickListener {
            val token = binding.tokenInput.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_invalid_token, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.validateBtn.isEnabled = false
            binding.validateBtn.text = getString(R.string.validating)

            viewLifecycleOwner.lifecycleScope.launch {
                val bot = TelegramApi.getMe(token)
                if (bot != null) {
                    prefs.token = token
                    prefs.botName = bot.name
                    bot.username?.let { prefs.botUsername = it }
                    updateConnectionStatus()
                    Toast.makeText(requireContext(), getString(R.string.connected_as, bot.username ?: bot.name), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), R.string.error_invalid_token, Toast.LENGTH_SHORT).show()
                    binding.connectedStatus.visibility = View.GONE
                }
                binding.validateBtn.isEnabled = true
                binding.validateBtn.text = getString(R.string.check_connection)
            }
        }

        updateConnectionStatus()
    }

    private fun updateConnectionStatus() {
        val name = prefs.botName
        val username = prefs.botUsername
        if (name.isNotEmpty() || username.isNotEmpty()) {
            binding.connectedStatus.visibility = View.VISIBLE
            val displayText = if (username.isNotEmpty()) {
                "${name.ifEmpty { "Bot" }} (@$username)"
            } else {
                name.ifEmpty { "Connected" }
            }
            binding.connectedText.text = displayText
        } else {
            binding.connectedStatus.visibility = View.GONE
        }
    }

    // ─── Lookup ─────────────────────────────────────────────────────

    private fun setupLookup() {
        binding.lookupBtn.setOnClickListener {
            val query = binding.lookupInput.text.toString().trim()
            if (query.isEmpty()) return@setOnClickListener

            val token = prefs.token
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_bot_token_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.lookupBtn.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                val result = TelegramApi.getChat(token, query)
                if (result.chat != null) {
                    val chat = result.chat
                    binding.lookupResult.visibility = View.VISIBLE

                    // Build display name: title > firstName lastName > username
                    val displayName = when {
                        !chat.title.isNullOrEmpty() -> chat.title
                        !chat.firstName.isNullOrEmpty() -> {
                            val fullName = chat.firstName + (chat.lastName?.let { " $it" } ?: "")
                            fullName
                        }
                        !chat.username.isNullOrEmpty() -> chat.username
                        else -> getString(R.string.unknown_chat)
                    }
                    binding.lookupTitle.text = displayName
                    binding.lookupType.text = chat.type
                    binding.lookupId.text = "ID: ${chat.id}"

                    if (!chat.username.isNullOrEmpty()) {
                        binding.lookupUsername.visibility = View.VISIBLE
                        binding.lookupUsername.text = "@${chat.username}"
                    } else {
                        binding.lookupUsername.visibility = View.GONE
                    }
                } else {
                    binding.lookupResult.visibility = View.GONE
                    val errorMsg = result.error ?: getString(R.string.not_found, query)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
                binding.lookupBtn.isEnabled = true
            }
        }
    }

    // ─── Targets (Channels) ─────────────────────────────────────────

    private fun setupTargets() {
        chatAdapter = MenuChatAdapter { chat ->
            prefs.removeChat(chat.id)
            refreshChannelsList()
        }
        binding.channelsList.layoutManager = LinearLayoutManager(requireContext())
        binding.channelsList.adapter = chatAdapter

        binding.addChannelBtn.setOnClickListener {
            val input = binding.newChannelInput.text.toString().trim()
            if (input.isEmpty()) return@setOnClickListener

            val token = prefs.token
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_bot_token_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existingChats = prefs.getChats()
            if (existingChats.any { it.id.toString() == input || it.title == input }) {
                Toast.makeText(requireContext(), R.string.chat_already_saved, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.addChannelBtn.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
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
                    refreshChannelsList()
                    binding.newChannelInput.text?.clear()
                    Toast.makeText(requireContext(), R.string.channel_added, Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = result.error ?: getString(R.string.not_found, input)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
                binding.addChannelBtn.isEnabled = true
            }
        }

        refreshChannelsList()
    }

    private fun refreshChannelsList() {
        val chats = prefs.getChats()
        chatAdapter.submitList(chats)

        if (chats.isEmpty()) {
            binding.noChannelsText.visibility = View.VISIBLE
            binding.channelsList.visibility = View.GONE
        } else {
            binding.noChannelsText.visibility = View.GONE
            binding.channelsList.visibility = View.VISIBLE
        }
    }

    // ─── Color Accent ───────────────────────────────────────────────

    private fun setupAccentSelector() {
        binding.accentEmerald.setOnClickListener { setAccent(0) }
        binding.accentBlue.setOnClickListener { setAccent(1) }
        binding.accentViolet.setOnClickListener { setAccent(2) }
        binding.accentRose.setOnClickListener { setAccent(3) }
        binding.accentAmber.setOnClickListener { setAccent(4) }
        updateAccentHighlight()
    }

    private fun setAccent(index: Int) {
        prefs.accent = index
        updateAccentHighlight()
        // Recreate activity to apply accent globally
        requireActivity().recreate()
    }

    private fun updateAccentHighlight() {
        val current = prefs.accent
        val dots = listOf(
            binding.accentEmerald,
            binding.accentBlue,
            binding.accentViolet,
            binding.accentRose,
            binding.accentAmber
        )
        dots.forEachIndexed { i, dot ->
            dot.alpha = if (i == current) 1.0f else 0.4f
            dot.scaleX = if (i == current) 1.2f else 1.0f
            dot.scaleY = if (i == current) 1.2f else 1.0f
        }

        binding.menuTitle.setTextColor(AccentColors.getPrimary(current))
    }

    // ─── Social Links ───────────────────────────────────────────────

    private fun setupSocialLinks() {
        binding.telegramLink.setOnClickListener {
            openUrl("https://t.me/JubairSensei")
        }

        binding.youtubeLink.setOnClickListener {
            openUrl("https://youtube.com/@JubairSensei")
        }
    }

    private fun setupJoinSupport() {
        binding.joinSupportBtn.setOnClickListener {
            openUrl("https://t.me/SenseiGramSupport")
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.error_connection, Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Disconnect ─────────────────────────────────────────────────

    private fun setupDisconnect() {
        binding.disconnectBtn.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.logout()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
