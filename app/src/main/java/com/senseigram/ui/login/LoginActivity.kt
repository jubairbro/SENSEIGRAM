package com.senseigram.ui.login

import android.content.Intent
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
import com.senseigram.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etToken: EditText
    private lateinit var btnConnect: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        etToken = findViewById(R.id.etToken)
        btnConnect = findViewById(R.id.btnConnect)
        progressBar = findViewById(R.id.progressBar)
        
        val savedToken = SenseiGramApp.prefs.getBotToken()
        if (savedToken.isNotEmpty()) {
            etToken.setText(savedToken)
        }
        
        btnConnect.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(this, R.string.enter_token, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            connectBot(token)
        }
    }
    
    private fun connectBot(token: String) {
        progressBar.visibility = android.view.View.VISIBLE
        btnConnect.isEnabled = false
        
        lifecycleScope.launch {
            val service = TelegramService(token)
            val result = service.getMe()
            
            result.fold(
                onSuccess = { user ->
                    SenseiGramApp.prefs.setBotToken(token)
                    SenseiGramApp.prefs.setBotUser("${user.first_name}|${user.username ?: ""}")
                    Toast.makeText(this@LoginActivity, getString(R.string.connected_as, user.first_name), Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                },
                onFailure = { error ->
                    Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                }
            )
            
            progressBar.visibility = android.view.View.GONE
            btnConnect.isEnabled = true
        }
    }
}
