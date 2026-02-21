package com.senseigram.ui.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.senseigram.R
import com.senseigram.data.*
import com.senseigram.databinding.ActivityComposeBinding
import com.senseigram.ui.adapters.ButtonsAdapter
import kotlinx.coroutines.launch
import java.util.UUID

class ComposeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityComposeBinding
    private lateinit var prefs: Prefs
    private lateinit var buttonsAdapter: ButtonsAdapter
    private var selectedFileUri: Uri? = null
    private var mediaType: MediaType = MediaType.TEXT
    private var actualChatId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComposeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = Prefs(this)
        
        actualChatId = intent.getStringExtra("chatId") ?: ""
        val chatName = intent.getStringExtra("chatName")
        
        // Show friendly name but keep real chat ID stored separately
        if (actualChatId.isNotEmpty() && chatName != null) {
            binding.chatIdInput.setText("$chatName ($actualChatId)")
        } else {
            binding.chatIdInput.setText(actualChatId)
        }
        
        setupViews()
    }
    
    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        buttonsAdapter = ButtonsAdapter()
        binding.buttonsRecycler.layoutManager = LinearLayoutManager(this)
        binding.buttonsRecycler.adapter = buttonsAdapter
        
        // When user manually types in chat ID field, clear the stored actualChatId
        // so we use the typed value instead
        binding.chatIdInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && actualChatId.isNotEmpty()) {
                binding.chatIdInput.setText(actualChatId)
                binding.chatIdInput.setSelection(actualChatId.length)
            }
        }
        
        binding.messageTypeGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            mediaType = when (checkedIds.firstOrNull()) {
                R.id.chipPhoto -> MediaType.PHOTO
                R.id.chipVideo -> MediaType.VIDEO
                R.id.chipDocument -> MediaType.DOCUMENT
                else -> MediaType.TEXT
            }
            binding.mediaSection.visibility = if (mediaType == MediaType.TEXT) View.GONE else View.VISIBLE
        }
        
        binding.selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = when (mediaType) {
                    MediaType.PHOTO -> "image/*"
                    MediaType.VIDEO -> "video/*"
                    else -> "*/*"
                }
            }
            startActivityForResult(Intent.createChooser(intent, "Select File"), 100)
        }
        
        binding.addButton.setOnClickListener {
            buttonsAdapter.addButton()
        }
        
        binding.saveDraftButton.setOnClickListener {
            saveDraft()
        }
        
        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            binding.selectedFileName.text = selectedFileUri?.lastPathSegment
            binding.selectedFileName.visibility = View.VISIBLE
        }
    }
    
    private fun resolveChatId(): String {
        // Use stored actualChatId if available, otherwise read from input
        return if (actualChatId.isNotEmpty()) {
            actualChatId
        } else {
            binding.chatIdInput.text.toString().trim()
        }
    }
    
    private fun sendMessage() {
        val chatId = resolveChatId()
        val text = binding.messageInput.text.toString().trim()
        val mediaUrl = binding.mediaUrlInput.text.toString().trim()
        val buttons = buttonsAdapter.getButtons()
        
        if (chatId.isEmpty()) {
            Toast.makeText(this, "Please enter chat ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (text.isEmpty() && mediaType == MediaType.TEXT) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }
        
        val silent = binding.silentSwitch.isChecked
        val protect = binding.protectSwitch.isChecked
        val spoiler = binding.spoilerSwitch.isChecked
        
        binding.progressBar.visibility = View.VISIBLE
        binding.sendButton.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val success = when (mediaType) {
                    MediaType.TEXT -> TelegramApi.sendMessage(prefs.token, chatId, text, buttons, silent, protect, false)
                    MediaType.PHOTO -> {
                        if (selectedFileUri != null) {
                            val file = getFileFromUri(selectedFileUri!!)
                            if (file != null) {
                                TelegramApi.sendPhoto(prefs.token, chatId, file, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                            } else false
                        } else if (mediaUrl.isNotEmpty()) {
                            TelegramApi.sendPhotoByUrl(prefs.token, chatId, mediaUrl, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                        } else false
                    }
                    MediaType.VIDEO -> {
                        if (selectedFileUri != null) {
                            val file = getFileFromUri(selectedFileUri!!)
                            if (file != null) {
                                TelegramApi.sendVideo(prefs.token, chatId, file, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                            } else false
                        } else if (mediaUrl.isNotEmpty()) {
                            TelegramApi.sendVideoByUrl(prefs.token, chatId, mediaUrl, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                        } else false
                    }
                    MediaType.DOCUMENT -> {
                        if (selectedFileUri != null) {
                            val file = getFileFromUri(selectedFileUri!!)
                            if (file != null) {
                                TelegramApi.sendDocument(prefs.token, chatId, file, text.ifEmpty { null }, buttons, silent, protect)
                            } else false
                        } else if (mediaUrl.isNotEmpty()) {
                            TelegramApi.sendDocumentByUrl(prefs.token, chatId, mediaUrl, text.ifEmpty { null }, buttons, silent, protect)
                        } else false
                    }
                }
                
                binding.progressBar.visibility = View.GONE
                binding.sendButton.isEnabled = true
                
                if (success) {
                    Toast.makeText(this@ComposeActivity, R.string.message_sent, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ComposeActivity, R.string.error_send, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.sendButton.isEnabled = true
                Toast.makeText(this@ComposeActivity, R.string.error_send, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun saveDraft() {
        val chatId = resolveChatId()
        val text = binding.messageInput.text.toString().trim()
        val buttons = buttonsAdapter.getButtons()
        
        val draft = MessageDraft(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            text = text,
            buttons = buttons
        )
        prefs.addDraft(draft)
        Toast.makeText(this, R.string.draft_saved, Toast.LENGTH_SHORT).show()
    }
    
    private fun getFileFromUri(uri: Uri): java.io.File? {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                java.io.File(cacheDir, "upload_${System.currentTimeMillis()}").also { file ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
