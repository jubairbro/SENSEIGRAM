package com.senseigram.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.data.SavedChat
import com.senseigram.databinding.ItemSavedChatBinding

class ChatsAdapter(
    private val onItemClick: (SavedChat) -> Unit,
    private val onEditClick: (SavedChat) -> Unit,
    private val onDeleteClick: (SavedChat) -> Unit
) : ListAdapter<SavedChat, ChatsAdapter.VH>(Diff) {
    
    inner class VH(val binding: ItemSavedChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedChat) {
            binding.chatNameText.text = item.title
            binding.chatIdText.text = "@${item.id}"
            binding.root.setOnClickListener { onItemClick(item) }
            binding.editButton.setOnClickListener { onEditClick(item) }
            binding.deleteButton.setOnClickListener { onDeleteClick(item) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSavedChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
    
    object Diff : DiffUtil.ItemCallback<SavedChat>() {
        override fun areItemsTheSame(old: SavedChat, new: SavedChat) = old.id == new.id
        override fun areContentsTheSame(old: SavedChat, new: SavedChat) = old == new
    }
}
