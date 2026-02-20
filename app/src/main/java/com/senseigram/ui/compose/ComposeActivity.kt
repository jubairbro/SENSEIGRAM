package com.senseigram.ui.compose

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.remote.TelegramService
import kotlinx.coroutines.launch

class ComposeActivity : AppCompatActivity() {
    
    private lateinit var etChatId: EditText
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)
        
        etChatId = findViewById(R.id.etChatId)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        progressBar = findViewById(R.id.progressBar)
        
        intent.getStringExtra("chatId")?.let { etChatId.setText(it) }
        intent.getStringExtra("chatTitle")?.let { supportActionBar?.title = it }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        btnSend.setOnClickListener {
            sendMessage()
        }
    }
    
    private fun sendMessage() {
        val chatId = etChatId.text.toString().trim()
        val message = etMessage.text.toString().trim()
        
        if (chatId.isEmpty()) {
            Toast.makeText(this, R.string.enter_chat_id, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (message.isEmpty()) {
            Toast.makeText(this, R.string.enter_message, Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = android.view.View.VISIBLE
        btnSend.isEnabled = false
        
        lifecycleScope.launch {
            val token = SenseiGramApp.prefs.getBotToken()
            val service = TelegramService(token)
            
            service.sendMessage(chatId, message).fold(
                onSuccess = {
                    Toast.makeText(this@ComposeActivity, R.string.message_sent, Toast.LENGTH_SHORT).show()
                    etMessage.text.clear()
                },
                onFailure = { error ->
                    Toast.makeText(this@ComposeActivity, error.message, Toast.LENGTH_LONG).show()
                }
            )
            
            progressBar.visibility = android.view.View.GONE
            btnSend.isEnabled = true
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
