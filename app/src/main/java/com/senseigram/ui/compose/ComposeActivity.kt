package com.senseigram.ui.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.senseigram.R
import com.senseigram.data.InlineBtn
import com.senseigram.data.Prefs
import com.senseigram.data.TelegramApi
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ComposeActivity : AppCompatActivity() {
    private var mediaUri: Uri? = null
    private var mediaType = 0
    private val buttons = mutableListOf<MutableList<InlineBtn>>()
    private lateinit var buttonsContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("title") ?: "Compose"
        
        val etChatId = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etChatId)
        val etMessage = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMessage)
        val btnSend = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSend)
        val progress = findViewById<android.widget.ProgressBar>(R.id.progress)
        val rgMedia = findViewById<android.widget.RadioGroup>(R.id.rgMedia)
        val btnAttach = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAttach)
        val tvAttachment = findViewById<android.widget.TextView>(R.id.tvAttachment)
        val btnAddButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddButton)
        buttonsContainer = findViewById(R.id.buttonsContainer)
        
        intent.getStringExtra("chatId")?.let { etChatId.setText(it) }
        
        rgMedia.setOnCheckedChangeListener { _, id ->
            mediaType = when (id) {
                R.id.rbPhoto -> 1
                R.id.rbVideo -> 2
                R.id.rbDoc -> 3
                else -> 0
            }
            btnAttach.visibility = if (mediaType > 0) View.VISIBLE else View.GONE
            mediaUri = null
            tvAttachment.visibility = View.GONE
        }
        
        btnAttach.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
                type = when (mediaType) { 1 -> "image/*"; 2 -> "video/*"; else -> "*/*" }
            }, 100)
        }
        
        btnAddButton.setOnClickListener { showAddButtonDialog() }
        
        btnSend.setOnClickListener {
            val chatId = etChatId.text.toString().trim()
            val text = etMessage.text.toString().trim()
            
            if (chatId.isEmpty()) {
                Toast.makeText(this, "Enter Chat ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            btnSend.isEnabled = false
            progress.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                val token = Prefs.token
                val btns = if (buttons.isNotEmpty()) buttons.toList() else null
                val success = when {
                    mediaType == 1 && mediaUri != null -> getFileFromUri(mediaUri!!)?.let { TelegramApi.sendPhoto(token, chatId, it, text.ifEmpty { null }, btns) } ?: false
                    mediaType == 2 && mediaUri != null -> getFileFromUri(mediaUri!!)?.let { TelegramApi.sendVideo(token, chatId, it, text.ifEmpty { null }, btns) } ?: false
                    mediaType == 3 && mediaUri != null -> getFileFromUri(mediaUri!!)?.let { TelegramApi.sendDocument(token, chatId, it, text.ifEmpty { null }, btns) } ?: false
                    else -> TelegramApi.sendMessage(token, chatId, text, btns)
                }
                
                if (success) {
                    Toast.makeText(this@ComposeActivity, "Sent!", Toast.LENGTH_SHORT).show()
                    etMessage.text?.clear()
                    mediaUri = null
                    tvAttachment.visibility = View.GONE
                } else {
                    Toast.makeText(this@ComposeActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
                btnSend.isEnabled = true
                progress.visibility = View.GONE
            }
        }
    }
    
    private fun showAddButtonDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_button, null)
        val etText = view.findViewById<EditText>(R.id.btnText)
        val etUrl = view.findViewById<EditText>(R.id.btnUrl)
        val etCallback = view.findViewById<EditText>(R.id.btnCallback)
        val rgStyle = view.findViewById<RadioGroup>(R.id.rgStyle)
        
        AlertDialog.Builder(this)
            .setTitle("Add Inline Button")
            .setView(view)
            .setPositiveButton("Add") { _, _ ->
                val text = etText.text.toString().trim()
                val url = etUrl.text.toString().trim().ifEmpty { null }
                val callback = etCallback.text.toString().trim().ifEmpty { null }
                val style = when (rgStyle.checkedRadioButtonId) {
                    R.id.stylePrimary -> "primary"
                    R.id.styleSuccess -> "success"
                    R.id.styleDanger -> "danger"
                    else -> "default"
                }
                
                if (text.isNotEmpty()) {
                    val btn = InlineBtn(text, url, callback, style)
                    if (buttons.isEmpty()) buttons.add(mutableListOf())
                    buttons.last().add(btn)
                    renderButtons()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun renderButtons() {
        buttonsContainer.removeAllViews()
        buttons.forEachIndexed { rowIndex, row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 8, 0, 0)
                }
            }
            row.forEachIndexed { btnIndex, btn ->
                val button = Button(this).apply {
                    text = btn.text
                    when (btn.style) {
                        "primary" -> setBackgroundColor(ContextCompat.getColor(context, R.color.btn_primary))
                        "success" -> setBackgroundColor(ContextCompat.getColor(context, R.color.btn_success))
                        "danger" -> setBackgroundColor(ContextCompat.getColor(context, R.color.btn_danger))
                    }
                    setTextColor(resources.getColor(R.color.white, null))
                    setOnLongClickListener {
                        AlertDialog.Builder(this@ComposeActivity)
                            .setTitle("Remove Button?")
                            .setPositiveButton("Yes") { _, _ ->
                                buttons[rowIndex].removeAt(btnIndex)
                                if (buttons[rowIndex].isEmpty()) buttons.removeAt(rowIndex)
                                renderButtons()
                            }
                            .setNegativeButton("No", null)
                            .show()
                        true
                    }
                }
                rowLayout.addView(button)
            }
            buttonsContainer.addView(rowLayout)
        }
    }
    
    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resCode, data)
        if (reqCode == 100 && resCode == Activity.RESULT_OK) {
            mediaUri = data?.data
            findViewById<TextView>(R.id.tvAttachment).apply {
                text = "File attached"
                visibility = View.VISIBLE
            }
        }
    }
    
    private fun getFileFromUri(uri: Uri): File? = try {
        contentResolver.openInputStream(uri)?.use { input ->
            File(cacheDir, "upload_${System.currentTimeMillis()}").apply {
                FileOutputStream(this).use { output -> input.copyTo(output) }
            }
        }
    } catch (e: Exception) { null }
    
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
