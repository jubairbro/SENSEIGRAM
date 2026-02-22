package com.senseigram.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.data.SavedChat
import com.senseigram.databinding.ItemMenuChatBinding

class MenuChatAdapter(
    private val onRemoveClick: (SavedChat) -> Unit
) : ListAdapter<SavedChat, MenuChatAdapter.VH>(Diff) {

    inner class VH(val binding: ItemMenuChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedChat) {
            binding.menuChatTitle.text = item.title
            binding.menuChatType.text = item.type
            binding.menuChatId.text = item.id.toString()
            binding.removeChatBtn.setOnClickListener { onRemoveClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMenuChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
