package com.senseigram.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.*
import com.senseigram.data.remote.TelegramService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ComposeFragment : Fragment() {
    
    private val viewModel: MainViewModel by activityViewModels()
    
    private lateinit var etChatId: TextInputEditText
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    
    private var selectedChatId: String? = null
    private var selectedMediaUri: Uri? = null
    private var mediaType: MediaType = MediaType.TEXT
    
    companion object {
        private const val REQUEST_MEDIA_PICK = 1001
        private const val REQUEST_PERMISSION = 1002
        
        fun newInstance(chatId: String? = null) = ComposeFragment().apply {
            arguments = Bundle().apply {
                putString("chatId", chatId)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_compose, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        selectedChatId = arguments?.getString("chatId")
        
        initViews(view)
        setupListeners(view)
    }
    
    private fun initViews(view: View) {
        etChatId = view.findViewById(R.id.etChatId)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        progressBar = view.findViewById(R.id.progressBar)
        
        selectedChatId?.let {
            etChatId.setText(it)
        }
    }
    
    private fun setupListeners(view: View) {
        val rgMedia = view.findViewById<RadioGroup>(R.id.rgMediaType)
        rgMedia.setOnCheckedChangeListener { _, checkedId ->
            mediaType = when (checkedId) {
                R.id.rbPhoto -> MediaType.PHOTO
                R.id.rbVideo -> MediaType.VIDEO
                R.id.rbDocument -> MediaType.DOCUMENT
                else -> MediaType.TEXT
            }
            
            val btnAttachMedia = view.findViewById<Button>(R.id.btnAttachMedia)
            btnAttachMedia.visibility = if (mediaType != MediaType.TEXT) View.VISIBLE else View.GONE
        }
        
        view.findViewById<Button>(R.id.btnAttachMedia).setOnClickListener {
            if (checkPermissions()) {
                openMediaPicker()
            } else {
                requestPermissions()
            }
        }
        
        view.findViewById<Button>(R.id.btnSaveDraft).setOnClickListener {
            saveDraft()
        }
        
        btnSend.setOnClickListener {
            sendMessage()
        }
    }
    
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_PERMISSION
        )
    }
    
    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = when (mediaType) {
                MediaType.PHOTO -> "image/*"
                MediaType.VIDEO -> "video/*"
                MediaType.DOCUMENT -> "*/*"
                else -> "*/*"
            }
        }
        startActivityForResult(intent, REQUEST_MEDIA_PICK)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MEDIA_PICK && resultCode == Activity.RESULT_OK) {
            selectedMediaUri = data?.data
            Toast.makeText(requireContext(), "Media attached", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveDraft() {
        val chatId = etChatId.text.toString().trim()
        val message = etMessage.text.toString().trim()
        
        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Message is empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        val draft = MessageDraft(
            id = System.currentTimeMillis().toString(),
            chatId = chatId,
            html = message,
            buttons = emptyList()
        )
        
        viewModel.addDraft(draft)
        Toast.makeText(requireContext(), "Draft saved", Toast.LENGTH_SHORT).show()
    }
    
    private fun sendMessage() {
        val chatId = etChatId.text.toString().trim()
        val message = etMessage.text.toString().trim()
        
        if (chatId.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter Chat ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            btnSend.isEnabled = false
            
            val token = SenseiGramApp.preferenceManager.botToken.first()
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Bot not connected", Toast.LENGTH_SHORT).show()
                return@launch
            }
            
            val service = TelegramService(token)
            
            val result = when (mediaType) {
                MediaType.TEXT -> service.sendMessage(chatId, message)
                MediaType.PHOTO -> {
                    selectedMediaUri?.let { uri ->
                        val file = getFileFromUri(uri)
                        if (file != null) {
                            service.sendPhoto(chatId, file, message)
                        } else {
                            Result.failure(Exception("Failed to read file"))
                        }
                    } ?: Result.failure(Exception("No media selected"))
                }
                MediaType.VIDEO -> {
                    selectedMediaUri?.let { uri ->
                        val file = getFileFromUri(uri)
                        if (file != null) {
                            service.sendVideo(chatId, file, message)
                        } else {
                            Result.failure(Exception("Failed to read file"))
                        }
                    } ?: Result.failure(Exception("No media selected"))
                }
                MediaType.DOCUMENT -> {
                    selectedMediaUri?.let { uri ->
                        val file = getFileFromUri(uri)
                        if (file != null) {
                            service.sendDocument(chatId, file, message)
                        } else {
                            Result.failure(Exception("Failed to read file"))
                        }
                    } ?: Result.failure(Exception("No media selected"))
                }
            }
            
            result.fold(
                onSuccess = {
                    Toast.makeText(requireContext(), "Sent successfully!", Toast.LENGTH_SHORT).show()
                    etMessage.text?.clear()
                    selectedMediaUri = null
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                }
            )
            
            progressBar.visibility = View.GONE
            btnSend.isEnabled = true
        }
    }
    
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "temp_${System.currentTimeMillis()}")
            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
