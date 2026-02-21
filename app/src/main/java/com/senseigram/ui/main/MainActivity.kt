package com.senseigram.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.senseigram.App
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.data.SavedChat
import com.senseigram.data.TelegramApi
import com.senseigram.ui.compose.ComposeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ChatsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        
        supportActionBar?.title = if (Prefs.botUsername.isNotEmpty()) "@${Prefs.botUsername}" else "SenseiGram"
        
        adapter = ChatsAdapter(
            onClick = { openCompose(it.id.toString(), it.title) },
            onDelete = { Prefs.removeChat(it.id); loadChats() }
        )
        
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvChats).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddDialog()
        }
        
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCompose).setOnClickListener {
            openCompose(null, null)
        }
        
        loadChats()
        showSubscription()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                Prefs.token = ""
                Prefs.botName = ""
                Prefs.botUsername = ""
                recreate()
                true
            }
            R.id.action_channel -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+5ygHfkZxVBc0Mjdl")))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun loadChats() {
        val chats = Prefs.getChats()
        adapter.submitList(chats)
        findViewById<android.widget.TextView>(R.id.tvEmpty).visibility = if (chats.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
    
    private fun showAddDialog() {
        val input = EditText(this).apply { hint = "@username or Chat ID" }
        AlertDialog.Builder(this)
            .setTitle("Add Chat")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val id = input.text.toString().trim()
                if (id.isNotEmpty()) addChat(id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addChat(chatId: String) {
        lifecycleScope.launch {
            var id = chatId
            if (!id.startsWith("@") && !id.startsWith("-") && id.toLongOrNull() == null) id = "@$id"
            val chat = TelegramApi.getChat(Prefs.token, id)
            if (chat != null) {
                Prefs.addChat(SavedChat(chat.id, chat.title ?: chat.username ?: "Unknown", chat.type))
                loadChats()
                Toast.makeText(this@MainActivity, "Added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Not found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun openCompose(chatId: String?, title: String?) {
        startActivity(Intent(this, ComposeActivity::class.java).apply {
            chatId?.let { putExtra("chatId", it) }
            title?.let { putExtra("title", it) }
        })
    }
    
    private fun showSubscription() {
        if (!Prefs.seenSub) {
            val view = layoutInflater.inflate(R.layout.dialog_subscription, null)
            val cb = view.findViewById<android.widget.CheckBox>(R.id.cbDontShow)
            AlertDialog.Builder(this).setView(view).setCancelable(false).create().apply {
                view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSubscribe).setOnClickListener {
                    Prefs.seenSub = cb.isChecked
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+5ygHfkZxVBc0Mjdl")))
                    dismiss()
                }
                view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLater).setOnClickListener {
                    Prefs.seenSub = cb.isChecked
                    dismiss()
                }
                show()
            }
        }
    }
}
