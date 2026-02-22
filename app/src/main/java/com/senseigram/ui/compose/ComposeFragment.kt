package com.senseigram.ui.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.senseigram.R
import com.senseigram.data.InlineBtn
import com.senseigram.data.MediaType
import com.senseigram.data.MessageDraft
import com.senseigram.data.Prefs
import com.senseigram.data.SavedChat
import com.senseigram.data.TelegramApi
import com.senseigram.databinding.FragmentComposeBinding
import com.senseigram.ui.adapters.ButtonRowAdapter
import com.senseigram.ui.main.MainActivity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.launch

class ComposeFragment : Fragment() {

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: Prefs
    private lateinit var buttonRowAdapter: ButtonRowAdapter

    private var isEditMode = false
    private var mediaType = MediaType.TEXT
    private var isUploadMode = true
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var editChatId: String = ""
    private var editMessageId: Long = 0L
    private var chats: List<SavedChat> = emptyList()

    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                selectedFileName = getFileName(uri)
                binding.fileNameText.text = selectedFileName ?: getString(R.string.choose_file)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComposeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        setupModeSwitcher()
        setupChannelSelector()
        setupMediaAttach()
        setupUploadLinkToggle()
        setupFormattingToolbar()
        setupInlineButtons()
        setupSendButton()
        setupSaveDraft()
        setupPostLinkParser()
    }

    override fun onResume() {
        super.onResume()
        refreshChannelSpinner()
    }

    // ─── Mode Switcher ──────────────────────────────────────────────

    private fun setupModeSwitcher() {
        updateModeUI()

        binding.newPostTab.setOnClickListener {
            isEditMode = false
            updateModeUI()
        }

        binding.editPostTab.setOnClickListener {
            isEditMode = true
            updateModeUI()
        }
    }

    private fun updateModeUI() {
        val accentColor = prefs.let { com.senseigram.data.AccentColors.getPrimary(it.accent) }

        if (!isEditMode) {
            binding.newPostTab.setBackgroundColor(accentColor)
            binding.newPostTab.setTextColor(resources.getColor(R.color.white, null))
            binding.editPostTab.setBackgroundColor(0)
            binding.editPostTab.setTextColor(resources.getColor(R.color.text_secondary_light, null))

            binding.newPostSection.visibility = View.VISIBLE
            binding.editPostSection.visibility = View.GONE
            binding.editOptionsSection.visibility = View.GONE
            binding.mediaAttachSection.visibility = View.VISIBLE
            binding.sendBtn.text = getString(R.string.send_now)
        } else {
            binding.editPostTab.setBackgroundColor(accentColor)
            binding.editPostTab.setTextColor(resources.getColor(R.color.white, null))
            binding.newPostTab.setBackgroundColor(0)
            binding.newPostTab.setTextColor(resources.getColor(R.color.text_secondary_light, null))

            binding.newPostSection.visibility = View.GONE
            binding.editPostSection.visibility = View.VISIBLE
            binding.editOptionsSection.visibility = View.VISIBLE
            binding.mediaAttachSection.visibility = View.GONE
            binding.mediaInputSection.visibility = View.GONE
            mediaType = MediaType.TEXT
            binding.sendBtn.text = getString(R.string.update_selected)
        }
    }

    // ─── Channel Selector ───────────────────────────────────────────

    private fun setupChannelSelector() {
        refreshChannelSpinner()
    }

    private fun refreshChannelSpinner() {
        chats = prefs.getChats()
        val items = mutableListOf(getString(R.string.select_channel))
        items.addAll(chats.map { it.title })
        items.add(getString(R.string.manual_input))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.channelSpinner.adapter = adapter

        binding.channelSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == items.size - 1) {
                    // Manual input
                    binding.manualChatIdInput.visibility = View.VISIBLE
                } else {
                    binding.manualChatIdInput.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun getSelectedChatId(): String {
        val pos = binding.channelSpinner.selectedItemPosition
        val items = (binding.channelSpinner.adapter?.count ?: 0)

        return when {
            pos == items - 1 -> binding.manualChatIdInput.text.toString().trim()
            pos > 0 && pos - 1 < chats.size -> chats[pos - 1].id.toString()
            else -> ""
        }
    }

    // ─── Post Link Parser ───────────────────────────────────────────

    private fun setupPostLinkParser() {
        binding.parseLinkBtn.setOnClickListener {
            val link = binding.postLinkInput.text.toString().trim()
            if (link.isEmpty()) return@setOnClickListener

            val parsed = parsePostLink(link)
            if (parsed != null) {
                editChatId = parsed.first
                editMessageId = parsed.second
                Toast.makeText(requireContext(), R.string.id_chat_extracted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.use_format, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parsePostLink(link: String): Pair<String, Long>? {
        // Format 1: https://t.me/c/1234567890/123
        val privateRegex = Regex("""t\.me/c/(\d+)/(\d+)""")
        val privateMatch = privateRegex.find(link)
        if (privateMatch != null) {
            val chatId = "-100${privateMatch.groupValues[1]}"
            val msgId = privateMatch.groupValues[2].toLongOrNull() ?: return null
            return Pair(chatId, msgId)
        }

        // Format 2: https://t.me/username/123
        val publicRegex = Regex("""t\.me/([a-zA-Z_][a-zA-Z0-9_]*)/(\d+)""")
        val publicMatch = publicRegex.find(link)
        if (publicMatch != null) {
            val username = "@${publicMatch.groupValues[1]}"
            val msgId = publicMatch.groupValues[2].toLongOrNull() ?: return null
            return Pair(username, msgId)
        }

        return null
    }

    // ─── Media Attach ───────────────────────────────────────────────

    private fun setupMediaAttach() {
        binding.attachPhoto.setOnClickListener { selectMediaType(MediaType.PHOTO) }
        binding.attachVideo.setOnClickListener { selectMediaType(MediaType.VIDEO) }
        binding.attachDocument.setOnClickListener { selectMediaType(MediaType.DOCUMENT) }
    }

    private fun selectMediaType(type: MediaType) {
        if (mediaType == type) {
            // Toggle off
            mediaType = MediaType.TEXT
            binding.mediaInputSection.visibility = View.GONE
            binding.mediaTypeLabel.visibility = View.GONE
            updateMediaButtonStates()
            updateEditorLabel()
            return
        }

        mediaType = type
        binding.mediaInputSection.visibility = View.VISIBLE
        binding.mediaTypeLabel.visibility = View.VISIBLE

        binding.mediaTypeLabel.text = when (type) {
            MediaType.PHOTO -> getString(R.string.sending_photo)
            MediaType.VIDEO -> getString(R.string.sending_video)
            MediaType.DOCUMENT -> getString(R.string.sending_file)
            else -> ""
        }

        // Show spoiler checkbox only for photo/video
        binding.spoilerCheck.visibility = if (type == MediaType.PHOTO || type == MediaType.VIDEO) View.VISIBLE else View.GONE

        updateMediaButtonStates()
        updateEditorLabel()
        updateUploadLinkMode()
    }

    private fun updateMediaButtonStates() {
        val accentColor = com.senseigram.data.AccentColors.getPrimary(prefs.accent)
        val defaultTint = resources.getColor(R.color.text_secondary_light, null)

        binding.attachPhoto.setColorFilter(if (mediaType == MediaType.PHOTO) accentColor else defaultTint)
        binding.attachVideo.setColorFilter(if (mediaType == MediaType.VIDEO) accentColor else defaultTint)
        binding.attachDocument.setColorFilter(if (mediaType == MediaType.DOCUMENT) accentColor else defaultTint)
    }

    private fun updateEditorLabel() {
        binding.editorLabel.text = if (mediaType == MediaType.TEXT) {
            getString(R.string.message_text)
        } else {
            getString(R.string.media_caption)
        }
    }

    // ─── Upload/Link Toggle ─────────────────────────────────────────

    private fun setupUploadLinkToggle() {
        binding.uploadToggle.setOnClickListener {
            isUploadMode = true
            updateUploadLinkMode()
        }

        binding.linkToggle.setOnClickListener {
            isUploadMode = false
            updateUploadLinkMode()
        }

        binding.pickFileBtn.setOnClickListener {
            val mimeType = when (mediaType) {
                MediaType.PHOTO -> "image/*"
                MediaType.VIDEO -> "video/*"
                else -> "*/*"
            }
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                this.type = mimeType
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            filePicker.launch(intent)
        }

        updateUploadLinkMode()
    }

    private fun updateUploadLinkMode() {
        val accentColor = com.senseigram.data.AccentColors.getPrimary(prefs.accent)
        val defaultTint = resources.getColor(R.color.text_secondary_light, null)

        if (isUploadMode) {
            binding.uploadToggle.setTextColor(accentColor)
            binding.linkToggle.setTextColor(defaultTint)
            binding.filePickerRow.visibility = View.VISIBLE
            binding.mediaUrlInput.visibility = View.GONE
        } else {
            binding.linkToggle.setTextColor(accentColor)
            binding.uploadToggle.setTextColor(defaultTint)
            binding.filePickerRow.visibility = View.GONE
            binding.mediaUrlInput.visibility = View.VISIBLE
        }
    }

    // ─── Formatting Toolbar ─────────────────────────────────────────

    private fun setupFormattingToolbar() {
        binding.btnBold.setOnClickListener { wrapSelectedText("<b>", "</b>") }
        binding.btnItalic.setOnClickListener { wrapSelectedText("<i>", "</i>") }
        binding.btnCode.setOnClickListener { wrapSelectedText("<code>", "</code>") }
        binding.btnLink.setOnClickListener { insertLinkTag() }
        binding.btnSpoiler.setOnClickListener { wrapSelectedText("<tg-spoiler>", "</tg-spoiler>") }
    }

    private fun wrapSelectedText(openTag: String, closeTag: String) {
        val start = binding.messageInput.selectionStart
        val end = binding.messageInput.selectionEnd
        val text = binding.messageInput.text ?: return

        if (start == end) {
            // No selection, insert tags at cursor
            text.insert(start, "$openTag$closeTag")
            binding.messageInput.setSelection(start + openTag.length)
        } else {
            // Wrap selection
            text.insert(end, closeTag)
            text.insert(start, openTag)
            binding.messageInput.setSelection(start + openTag.length, end + openTag.length)
        }
    }

    private fun insertLinkTag() {
        val start = binding.messageInput.selectionStart
        val end = binding.messageInput.selectionEnd
        val text = binding.messageInput.text ?: return

        if (start == end) {
            val linkTemplate = "<a href=\"URL\">text</a>"
            text.insert(start, linkTemplate)
            binding.messageInput.setSelection(start + 9, start + 12) // Select "URL"
        } else {
            val selectedText = text.subSequence(start, end).toString()
            val replacement = "<a href=\"URL\">$selectedText</a>"
            text.replace(start, end, replacement)
            binding.messageInput.setSelection(start + 9, start + 12) // Select "URL"
        }
    }

    // ─── Inline Buttons Builder ─────────────────────────────────────

    private fun setupInlineButtons() {
        buttonRowAdapter = ButtonRowAdapter { position ->
            buttonRowAdapter.removeRow(position)
        }
        binding.buttonsPreviewRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.buttonsPreviewRecycler.adapter = buttonRowAdapter

        binding.addToRowBtn.setOnClickListener {
            val label = binding.btnLabelInput.text.toString().trim()
            val url = binding.btnUrlInput.text.toString().trim()
            if (label.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_fields_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val btn = InlineBtn(label, url.ifEmpty { null }, null, 0)
            buttonRowAdapter.addButtonToLastRow(btn)
            binding.btnLabelInput.text?.clear()
            binding.btnUrlInput.text?.clear()
        }

        binding.newRowBtn.setOnClickListener {
            val label = binding.btnLabelInput.text.toString().trim()
            val url = binding.btnUrlInput.text.toString().trim()
            if (label.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_fields_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val btn = InlineBtn(label, url.ifEmpty { null }, null, 0)
            buttonRowAdapter.addButtonToNewRow(btn)
            binding.btnLabelInput.text?.clear()
            binding.btnUrlInput.text?.clear()
        }
    }

    // ─── Save Draft ─────────────────────────────────────────────────

    private fun setupSaveDraft() {
        binding.saveDraftBtn.setOnClickListener {
            val text = binding.messageInput.text.toString()
            val chatId = if (isEditMode) editChatId else getSelectedChatId()

            if (text.isEmpty() && buttonRowAdapter.getRows().isEmpty()) {
                Toast.makeText(requireContext(), R.string.content_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val draft = MessageDraft(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                text = text,
                buttons = buttonRowAdapter.getRows(),
                timestamp = System.currentTimeMillis()
            )
            prefs.addDraft(draft)
            Toast.makeText(requireContext(), R.string.saved_to_drafts, Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Send / Update ──────────────────────────────────────────────

    private fun setupSendButton() {
        binding.sendBtn.setOnClickListener {
            if (isEditMode) {
                handleEditPost()
            } else {
                handleNewPost()
            }
        }
    }

    private fun handleNewPost() {
        val token = prefs.token
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_bot_token_required, Toast.LENGTH_SHORT).show()
            return
        }

        val chatId = getSelectedChatId()
        if (chatId.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_select_channel, Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.messageInput.text.toString()
        val buttons = buttonRowAdapter.getRows().ifEmpty { null }
        val silent = binding.silentCheck.isChecked
        val protect = binding.protectCheck.isChecked
        val hidePreview = binding.hidePreviewCheck.isChecked
        val spoiler = binding.spoilerCheck.isChecked

        if (mediaType == MediaType.TEXT && text.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_enter_message, Toast.LENGTH_SHORT).show()
            return
        }

        setSending(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = when (mediaType) {
                MediaType.TEXT -> {
                    TelegramApi.sendMessage(token, chatId, text, buttons, silent, protect, hidePreview)
                }
                MediaType.PHOTO -> {
                    if (isUploadMode && selectedFileUri != null) {
                        val file = uriToFile(selectedFileUri!!) ?: run {
                            setSending(false)
                            Toast.makeText(requireContext(), R.string.error_read_file, Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        TelegramApi.sendPhoto(token, chatId, file, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                    } else {
                        val url = binding.mediaUrlInput.text.toString().trim()
                        if (url.isEmpty()) {
                            setSending(false)
                            Toast.makeText(requireContext(), R.string.error_add_file_or_text, Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        TelegramApi.sendPhotoByUrl(token, chatId, url, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                    }
                }
                MediaType.VIDEO -> {
                    if (isUploadMode && selectedFileUri != null) {
                        val file = uriToFile(selectedFileUri!!) ?: run {
                            setSending(false)
                            Toast.makeText(requireContext(), R.string.error_read_file, Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        TelegramApi.sendVideo(token, chatId, file, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                    } else {
                        val url = binding.mediaUrlInput.text.toString().trim()
                        if (url.isEmpty()) {
                            setSending(false)
                            Toast.makeText(requireContext(), R.string.error_add_file_or_text, Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        TelegramApi.sendVideoByUrl(token, chatId, url, text.ifEmpty { null }, buttons, silent, protect, spoiler)
                    }
                }
                MediaType.DOCUMENT -> {
                    if (isUploadMode && selectedFileUri != null) {
                        val file = uriToFile(selectedFileUri!!) ?: run {
                            setSending(false)
                            Toast.makeText(requireContext(), R.string.error_read_file, Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        TelegramApi.sendDocument(token, chatId, file, text.ifEmpty { null }, buttons, silent, protect)
                    } else {
                        val url = binding.mediaUrlInput.text.toString().trim()
                        if (url.isEmpty()) {
                            setSending(false)
                            Toast.makeText(requireContext(), R.string.error_add_file_or_text, Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        TelegramApi.sendDocumentByUrl(token, chatId, url, text.ifEmpty { null }, buttons, silent, protect)
                    }
                }
            }

            setSending(false)

            result.onSuccess {
                Toast.makeText(requireContext(), R.string.message_sent, Toast.LENGTH_SHORT).show()
                clearForm()
            }.onFailure { e ->
                Toast.makeText(requireContext(), "${getString(R.string.error_send)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleEditPost() {
        val token = prefs.token
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_bot_token_required, Toast.LENGTH_SHORT).show()
            return
        }

        if (editChatId.isEmpty() || editMessageId == 0L) {
            Toast.makeText(requireContext(), R.string.msg_id_missing, Toast.LENGTH_SHORT).show()
            return
        }

        val editText = binding.editTextCheck.isChecked
        val editButtons = binding.editButtonsCheck.isChecked

        if (!editText && !editButtons) {
            Toast.makeText(requireContext(), R.string.nothing_to_update, Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.messageInput.text.toString()
        val buttons = buttonRowAdapter.getRows()

        setSending(true)

        viewLifecycleOwner.lifecycleScope.launch {
            var success = true
            var errorMsg = ""

            // Update text/caption
            if (editText) {
                val result = if (mediaType == MediaType.TEXT) {
                    TelegramApi.editMessageText(token, editChatId, editMessageId, text, if (editButtons) buttons else null)
                } else {
                    TelegramApi.editMessageCaption(token, editChatId, editMessageId, text, if (editButtons) buttons else null)
                }
                result.onFailure { e ->
                    success = false
                    errorMsg = e.message ?: getString(R.string.error_send)
                }
            }

            // Update buttons only (if not already done with text)
            if (editButtons && !editText && success) {
                val result = TelegramApi.editMessageReplyMarkup(token, editChatId, editMessageId, buttons)
                result.onFailure { e ->
                    success = false
                    errorMsg = e.message ?: getString(R.string.error_send)
                }
            }

            setSending(false)

            if (success) {
                Toast.makeText(requireContext(), R.string.message_updated, Toast.LENGTH_SHORT).show()
                clearForm()
            } else {
                Toast.makeText(requireContext(), "${getString(R.string.error_send)}: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private fun setSending(sending: Boolean) {
        binding.sendProgress.visibility = if (sending) View.VISIBLE else View.GONE
        binding.sendBtn.isEnabled = !sending
        binding.sendBtn.text = if (sending) getString(R.string.sending) else {
            if (isEditMode) getString(R.string.update_selected) else getString(R.string.send_now)
        }
    }

    private fun clearForm() {
        binding.messageInput.text?.clear()
        binding.mediaUrlInput.text?.clear()
        binding.manualChatIdInput.text?.clear()
        binding.postLinkInput.text?.clear()
        binding.btnLabelInput.text?.clear()
        binding.btnUrlInput.text?.clear()
        buttonRowAdapter.clear()
        selectedFileUri = null
        selectedFileName = null
        binding.fileNameText.text = getString(R.string.choose_file)
        binding.silentCheck.isChecked = false
        binding.protectCheck.isChecked = false
        binding.hidePreviewCheck.isChecked = false
        binding.spoilerCheck.isChecked = false
        mediaType = MediaType.TEXT
        binding.mediaInputSection.visibility = View.GONE
        binding.mediaTypeLabel.visibility = View.GONE
        updateMediaButtonStates()
        updateEditorLabel()
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val ctx = requireContext()
            val inputStream = ctx.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(uri) ?: "file"
            val tempFile = File(ctx.cacheDir, fileName)
            FileOutputStream(tempFile).use { out ->
                inputStream.copyTo(out)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
