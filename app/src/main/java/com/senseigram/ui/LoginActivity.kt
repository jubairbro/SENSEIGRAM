package com.senseigram.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.data.TelegramApi
import com.senseigram.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Prefs.token.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_login)
        
        val etToken = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etToken)
        val btnConnect = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConnect)
        val progress = findViewById<android.widget.ProgressBar>(R.id.progress)
        
        btnConnect.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(this, "Enter bot token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            btnConnect.isEnabled = false
            progress.visibility = android.view.View.VISIBLE
            
            lifecycleScope.launch {
                val bot = TelegramApi.getMe(token)
                if (bot != null) {
                    Prefs.token = token
                    Prefs.botName = bot.name
                    Prefs.botUsername = bot.username ?: ""
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid token", Toast.LENGTH_SHORT).show()
                }
                btnConnect.isEnabled = true
                progress.visibility = android.view.View.GONE
            }
        }
    }
}
