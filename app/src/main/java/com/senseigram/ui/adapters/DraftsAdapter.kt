package com.senseigram.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.data.MessageDraft
import com.senseigram.databinding.ItemDraftBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DraftsAdapter(
    private val onItemClick: (MessageDraft) -> Unit,
    private val onDeleteClick: (MessageDraft) -> Unit
) : ListAdapter<MessageDraft, DraftsAdapter.VH>(Diff) {
    
    inner class VH(val binding: ItemDraftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageDraft) {
            binding.draftChatIdText.text = item.chatId.ifEmpty { "Unknown" }
            binding.draftPreviewText.text = item.text.ifEmpty { "(Empty message)" }
            binding.root.setOnClickListener { onItemClick(item) }
            binding.deleteDraftButton.setOnClickListener { onDeleteClick(item) }
            binding.editDraftButton.setOnClickListener { onItemClick(item) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDraftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
    
    object Diff : DiffUtil.ItemCallback<MessageDraft>() {
        override fun areItemsTheSame(old: MessageDraft, new: MessageDraft) = old.id == new.id
        override fun areContentsTheSame(old: MessageDraft, new: MessageDraft) = old == new
    }
}
