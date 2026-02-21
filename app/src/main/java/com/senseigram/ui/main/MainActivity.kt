package com.senseigram.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.data.SavedChat
import com.senseigram.data.TelegramApi
import com.senseigram.databinding.ActivityMainBinding
import com.senseigram.databinding.DialogSaveChatBinding
import com.senseigram.ui.adapters.ChatsAdapter
import com.senseigram.ui.adapters.DraftsAdapter
import com.senseigram.ui.compose.ComposeActivity
import com.senseigram.ui.login.LoginActivity
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs
    private lateinit var chatsAdapter: ChatsAdapter
    private lateinit var draftsAdapter: DraftsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = Prefs(this)
        
        if (prefs.token.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        setupViews()
        loadData()
    }
    
    private fun setupViews() {
        binding.welcomeCard.visibility = View.VISIBLE
        binding.welcomeText.text = "${getString(R.string.welcome)}, ${prefs.botName}!"
        binding.botNameText.text = if (prefs.botUsername.isNotEmpty()) "@${prefs.botUsername}" else ""
        
        chatsAdapter = ChatsAdapter(
            onItemClick = { chat -> openCompose(chat.id.toString(), chat.title) },
            onEditClick = { chat -> showChatDialog(chat) },
            onDeleteClick = { chat -> confirmDeleteChat(chat) }
        )
        binding.savedChatsRecycler.layoutManager = LinearLayoutManager(this)
        binding.savedChatsRecycler.adapter = chatsAdapter
        
        draftsAdapter = DraftsAdapter(
            onItemClick = { draft -> openCompose(draft.chatId, null) },
            onDeleteClick = { draft -> prefs.removeDraft(draft.id); loadData() }
        )
        binding.draftsRecycler.layoutManager = LinearLayoutManager(this)
        binding.draftsRecycler.adapter = draftsAdapter
        
        binding.addChatButton.setOnClickListener { showChatDialog(null) }
        binding.composeFab.setOnClickListener { openCompose("", null) }
        
        binding.settingsButton.setOnClickListener { showSettingsMenu() }
    }
    
    private fun loadData() {
        val chats = prefs.getChats()
        chatsAdapter.submitList(chats)
        
        if (chats.isEmpty()) {
            binding.noChatsText.visibility = View.VISIBLE
            binding.noChatsDescText.visibility = View.VISIBLE
        } else {
            binding.noChatsText.visibility = View.GONE
            binding.noChatsDescText.visibility = View.GONE
        }
        
        val drafts = prefs.getDrafts()
        draftsAdapter.submitList(drafts)
        binding.noDraftsText.visibility = if (drafts.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun openCompose(chatId: String, chatName: String?) {
        val intent = Intent(this, ComposeActivity::class.java)
        intent.putExtra("chatId", chatId)
        chatName?.let { intent.putExtra("chatName", it) }
        startActivity(intent)
    }
    
    private fun showChatDialog(existing: SavedChat?) {
        val dialogBinding = DialogSaveChatBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        existing?.let {
            dialogBinding.nameInput.setText(it.title)
            dialogBinding.chatIdInput.setText(it.id.toString())
        }
        
        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.saveButton.setOnClickListener {
            val name = dialogBinding.nameInput.text.toString().trim()
            val chatId = dialogBinding.chatIdInput.text.toString().trim()
            
            if (name.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                val chat = TelegramApi.getChat(prefs.token, chatId)
                if (chat != null) {
                    val savedChat = SavedChat(
                        id = chat.id,
                        title = name,
                        type = chat.type,
                        time = System.currentTimeMillis()
                    )
                    prefs.addChat(savedChat)
                    loadData()
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity, "Chat saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid chat ID", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    
    private fun confirmDeleteChat(chat: SavedChat) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setMessage("Delete ${chat.title}?")
            .setPositiveButton(R.string.delete) { _, _ ->
                prefs.removeChat(chat.id)
                loadData()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showSettingsMenu() {
        val items = arrayOf(
            getString(R.string.disconnect)
        )
        AlertDialog.Builder(this)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> disconnectBot()
                }
            }
            .show()
    }
    
    private fun disconnectBot() {
        prefs.token = ""
        prefs.botName = ""
        prefs.botUsername = ""
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        loadData()
    }
}
