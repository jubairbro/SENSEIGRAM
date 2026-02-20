package com.senseigram.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.SavedChat
import com.senseigram.data.remote.TelegramService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SavedChatsActivity : AppCompatActivity() {
    
    private lateinit var adapter: com.senseigram.ui.adapters.SavedChatsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_chats)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saved Chats"
        
        setupRecyclerView()
        setupFab()
        observeData()
    }
    
    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvChats)
        adapter = com.senseigram.ui.adapters.SavedChatsAdapter(
            onItemClick = { chat ->
                finishWithResult(chat)
            },
            onDeleteClick = { chat ->
                showDeleteConfirmation(chat)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddChat).setOnClickListener {
            showAddChatDialog()
        }
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            SenseiGramApp.preferenceManager.savedChats.collect { chats ->
                adapter.submitList(chats)
            }
        }
    }
    
    private fun showAddChatDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_chat, null)
        val etChatId = view.findViewById<TextInputEditText>(R.id.etChatId)
        val etTitle = view.findViewById<TextInputEditText>(R.id.etTitle)
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Chat")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val chatId = etChatId.text.toString().trim()
                val title = etTitle.text.toString().trim()
                
                if (chatId.isNotEmpty()) {
                    addChat(chatId, title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addChat(chatId: String, title: String) {
        lifecycleScope.launch {
            val token = SenseiGramApp.preferenceManager.botToken.first()
            if (token.isEmpty()) {
                return@launch
            }
            
            var targetId = chatId
            if (!chatId.startsWith("@") && !chatId.startsWith("-") && chatId.toLongOrNull() == null) {
                targetId = "@$chatId"
            }
            
            val service = TelegramService(token)
            service.getChat(targetId).fold(
                onSuccess = { chat ->
                    val savedChat = SavedChat(
                        id = chat.id,
                        title = title.ifEmpty { chat.title ?: chat.username ?: "Unknown" },
                        type = chat.type
                    )
                    SenseiGramApp.preferenceManager.addSavedChat(savedChat)
                },
                onFailure = {
                    // Handle error
                }
            )
        }
    }
    
    private fun showDeleteConfirmation(chat: SavedChat) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove Chat")
            .setMessage("Remove ${chat.title} from saved chats?")
            .setPositiveButton("Remove") { _, _ ->
                lifecycleScope.launch {
                    SenseiGramApp.preferenceManager.removeSavedChat(chat.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun finishWithResult(chat: SavedChat) {
        // Handle click if needed
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
