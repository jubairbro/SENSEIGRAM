package com.senseigram.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.Announcement
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val ANNOUNCEMENT_URL = "https://raw.githubusercontent.com/jubairbro/Faw/refs/heads/main/folder/announcement.json"
        private const val CHANNEL_LINK = "https://t.me/+5ygHfkZxVBc0Mjdl"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        lifecycleScope.launch {
            val hasSeenSubscription = SenseiGramApp.preferenceManager.hasSeenSubscription.first()
            
            if (!hasSeenSubscription) {
                showSubscriptionDialog()
            } else {
                checkAnnouncementAndProceed()
            }
        }
    }
    
    private fun showSubscriptionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_subscription, null)
        val dontShowAgain = dialogView.findViewById<CheckBox>(R.id.cbDontShowAgain)
        
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        dialogView.findViewById<Button>(R.id.btnSubscribe).setOnClickListener {
            lifecycleScope.launch {
                SenseiGramApp.preferenceManager.setHasSeenSubscription(dontShowAgain.isChecked)
            }
            openChannel()
            dialog.dismiss()
            proceedToMain()
        }
        
        dialogView.findViewById<Button>(R.id.btnLater).setOnClickListener {
            lifecycleScope.launch {
                SenseiGramApp.preferenceManager.setHasSeenSubscription(dontShowAgain.isChecked)
            }
            dialog.dismiss()
            checkAnnouncementAndProceed()
        }
        
        dialog.show()
    }
    
    private fun openChannel() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CHANNEL_LINK))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun checkAnnouncementAndProceed() {
        lifecycleScope.launch {
            try {
                val announcement = fetchAnnouncement()
                if (announcement?.show == true) {
                    showAnnouncementDialog(announcement)
                } else {
                    proceedToMain()
                }
            } catch (e: Exception) {
                proceedToMain()
            }
        }
    }
    
    private suspend fun fetchAnnouncement(): Announcement? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(ANNOUNCEMENT_URL).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext null
                Gson().fromJson(body, Announcement::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun showAnnouncementDialog(announcement: Announcement) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_announcement, null)
        dialogView.findViewById<TextView>(R.id.tvTitle).text = announcement.title
        dialogView.findViewById<TextView>(R.id.tvMessage).text = announcement.text
        
        val dialog = AlertDialog.Builder(this, R.style.DialogStyle)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        dialogView.findViewById<Button>(R.id.btnAction).apply {
            if (!announcement.buttonText.isNullOrEmpty() && !announcement.buttonUrl.isNullOrEmpty()) {
                text = announcement.buttonText
                setOnClickListener {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(announcement.buttonUrl)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dialog.dismiss()
                    proceedToMain()
                }
            } else {
                visibility = View.GONE
            }
        }
        
        dialogView.findViewById<Button>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
            proceedToMain()
        }
        
        dialog.setOnCancelListener {
            proceedToMain()
        }
        
        dialog.show()
    }
    
    private fun proceedToMain() {
        lifecycleScope.launch {
            val hasToken = SenseiGramApp.preferenceManager.botToken.first().isNotEmpty()
            val intent = if (hasToken) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}
