package com.senseigram.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.remote.TelegramService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etToken: EditText
    private lateinit var btnConnect: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        etToken = findViewById(R.id.etBotToken)
        btnConnect = findViewById(R.id.btnConnect)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        
        btnConnect.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token.isEmpty()) {
                tvError.text = "Please enter your bot token"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            
            validateAndConnect(token)
        }
    }
    
    private fun validateAndConnect(token: String) {
        progressBar.visibility = View.VISIBLE
        btnConnect.isEnabled = false
        tvError.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val service = TelegramService(token)
                val result = service.getMe()
                
                result.fold(
                    onSuccess = { user ->
                        SenseiGramApp.preferenceManager.setBotToken(token)
                        Toast.makeText(
                            this@LoginActivity,
                            "Connected as @${user.username ?: user.first_name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { error ->
                        tvError.text = error.message ?: "Invalid token"
                        tvError.visibility = View.VISIBLE
                    }
                )
            } catch (e: Exception) {
                tvError.text = e.message ?: "Connection failed"
                tvError.visibility = View.VISIBLE
            } finally {
                progressBar.visibility = View.GONE
                btnConnect.isEnabled = true
            }
        }
    }
}
