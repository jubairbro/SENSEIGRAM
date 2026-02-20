package com.senseigram.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.SavedChat
import com.senseigram.data.remote.TelegramService
import com.senseigram.ui.compose.ComposeActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    
    private lateinit var tvBotInfo: TextView
    private lateinit var rvChats: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: ChatsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        
        tvBotInfo = findViewById(R.id.tvBotInfo)
        rvChats = findViewById(R.id.rvChats)
        fabAdd = findViewById(R.id.fabAdd)
        
        setupRecyclerView()
        loadBotInfo()
        loadChats()
        
        fabAdd.setOnClickListener {
            showAddChatDialog()
        }
        
        findViewById<Button>(R.id.btnCompose).setOnClickListener {
            startActivity(Intent(this, ComposeActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnDisconnect).setOnClickListener {
            disconnect()
        }
        
        checkSubscription()
    }
    
    private fun setupRecyclerView() {
        adapter = ChatsAdapter(
            onClick = { chat ->
                val intent = Intent(this, ComposeActivity::class.java)
                intent.putExtra("chatId", chat.id.toString())
                intent.putExtra("chatTitle", chat.title)
                startActivity(intent)
            },
            onDelete = { chat ->
                SenseiGramApp.prefs.removeSavedChat(chat.id)
                loadChats()
            }
        )
        rvChats.layoutManager = LinearLayoutManager(this)
        rvChats.adapter = adapter
    }
    
    private fun loadBotInfo() {
        val botUser = SenseiGramApp.prefs.getBotUser()
        if (botUser.isNotEmpty()) {
            val parts = botUser.split("|")
            val name = parts.getOrElse(0) { "Bot" }
            val username = parts.getOrElse(1) { "" }
            tvBotInfo.text = if (username.isNotEmpty()) "@$username" else name
        }
    }
    
    private fun loadChats() {
        val chats = SenseiGramApp.prefs.getSavedChats()
        adapter.submitList(chats)
        
        findViewById<TextView>(R.id.tvEmpty).visibility = 
            if (chats.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
    
    private fun showAddChatDialog() {
        val input = android.widget.EditText(this)
        input.hint = getString(R.string.chat_id_or_username)
        
        AlertDialog.Builder(this)
            .setTitle(R.string.add_chat)
            .setView(input)
            .setPositiveButton(R.string.add) { _, _ ->
                val chatId = input.text.toString().trim()
                if (chatId.isNotEmpty()) {
                    addChat(chatId)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun addChat(chatId: String) {
        lifecycleScope.launch {
            val token = SenseiGramApp.prefs.getBotToken()
            val service = TelegramService(token)
            
            var targetId = chatId
            if (!chatId.startsWith("@") && !chatId.startsWith("-") && chatId.toLongOrNull() == null) {
                targetId = "@$chatId"
            }
            
            service.getChat(targetId).fold(
                onSuccess = { chat ->
                    val savedChat = SavedChat(
                        id = chat.id,
                        title = chat.title ?: chat.username ?: "Unknown",
                        type = chat.type
                    )
                    SenseiGramApp.prefs.addSavedChat(savedChat)
                    loadChats()
                    Toast.makeText(this@HomeActivity, R.string.chat_added, Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
    
    private fun disconnect() {
        SenseiGramApp.prefs.setBotToken("")
        SenseiGramApp.prefs.setBotUser("")
        val intent = Intent(this, com.senseigram.ui.login.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun checkSubscription() {
        if (!SenseiGramApp.prefs.hasSeenSubscription()) {
            val view = layoutInflater.inflate(R.layout.dialog_subscription, null)
            val cbDontShow = view.findViewById<android.widget.CheckBox>(R.id.cbDontShow)
            
            AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
                .apply {
                    view.findViewById<Button>(R.id.btnSubscribe).setOnClickListener {
                        SenseiGramApp.prefs.setSeenSubscription(cbDontShow.isChecked)
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+5ygHfkZxVBc0Mjdl")))
                        dismiss()
                    }
                    view.findViewById<Button>(R.id.btnLater).setOnClickListener {
                        SenseiGramApp.prefs.setSeenSubscription(cbDontShow.isChecked)
                        dismiss()
                    }
                    show()
                }
        }
    }
}
