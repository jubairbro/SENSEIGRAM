package com.senseigram.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.data.TelegramApi
import com.senseigram.databinding.ActivityLoginBinding
import com.senseigram.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)

        if (prefs.token.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.connectButton.setOnClickListener {
            val token = binding.tokenInput.text.toString().trim()
            if (token.isEmpty()) {
                binding.tokenInputLayout.error = getString(R.string.error_invalid_token)
                return@setOnClickListener
            }
            binding.tokenInputLayout.error = null
            connectBot(token)
        }
    }

    private fun connectBot(token: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.connectButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val bot = TelegramApi.getMe(token)
                if (bot != null) {
                    prefs.token = token
                    prefs.botName = bot.name
                    bot.username?.let { prefs.botUsername = it }
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, R.string.error_invalid_token, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, R.string.error_connection, Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.connectButton.isEnabled = true
            }
        }
    }
}
