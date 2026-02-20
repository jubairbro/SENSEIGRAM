package com.senseigram.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.BotInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    
    private lateinit var etToken: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        etToken = findViewById(R.id.etToken)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        
        checkLogin()
        
        btnConnect.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token.isNotEmpty()) {
                connectBot(token)
            }
        }
        
        btnDisconnect.setOnClickListener {
            SenseiGramApp.prefs.botToken = ""
            SenseiGramApp.prefs.botName = ""
            checkLogin()
        }
    }
    
    private fun checkLogin() {
        val token = SenseiGramApp.prefs.botToken
        if (token.isNotEmpty()) {
            etToken.visibility = android.view.View.GONE
            btnConnect.visibility = android.view.View.GONE
            btnDisconnect.visibility = android.view.View.VISIBLE
            findViewById<android.widget.TextView>(R.id.tvStatus).text = 
                "Connected: @${SenseiGramApp.prefs.botUsername}"
            showSubscriptionDialog()
        } else {
            etToken.visibility = android.view.View.VISIBLE
            btnConnect.visibility = android.view.View.VISIBLE
            btnDisconnect.visibility = android.view.View.GONE
            findViewById<android.widget.TextView>(R.id.tvStatus).text = "Not connected"
        }
    }
    
    private fun connectBot(token: String) {
        lifecycleScope.launch {
            try {
                val bot = validateToken(token)
                if (bot != null) {
                    SenseiGramApp.prefs.botToken = token
                    SenseiGramApp.prefs.botName = bot.firstName
                    SenseiGramApp.prefs.botUsername = bot.username ?: ""
                    Toast.makeText(this@MainActivity, "Connected as ${bot.firstName}", Toast.LENGTH_SHORT).show()
                    checkLogin()
                } else {
                    Toast.makeText(this@MainActivity, "Invalid token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private suspend fun validateToken(token: String): BotInfo? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.telegram.org/bot$token/getMe")
            .build()
        
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext null
        
        val json = JSONObject(body)
        if (json.getBoolean("ok")) {
            val result = json.getJSONObject("result")
            BotInfo(
                token = token,
                firstName = result.getString("first_name"),
                username = result.optString("username")
            )
        } else null
    }
    
    private fun showSubscriptionDialog() {
        if (!SenseiGramApp.prefs.seenSub) {
            val view = layoutInflater.inflate(R.layout.dialog_subscription, null)
            val cbDontShow = view.findViewById<CheckBox>(R.id.cbDontShow)
            
            AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
                .apply {
                    view.findViewById<Button>(R.id.btnSubscribe).setOnClickListener {
                        SenseiGramApp.prefs.seenSub = cbDontShow.isChecked
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+5ygHfkZxVBc0Mjdl")))
                        dismiss()
                    }
                    view.findViewById<Button>(R.id.btnLater).setOnClickListener {
                        SenseiGramApp.prefs.seenSub = cbDontShow.isChecked
                        dismiss()
                    }
                    show()
                }
        }
    }
}
