package com.senseigram.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.data.model.SavedChat

class SavedChatsAdapter(
    private val onItemClick: (SavedChat) -> Unit,
    private val onDeleteClick: (SavedChat) -> Unit
) : ListAdapter<SavedChat, SavedChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_chat, parent, false)
        return ChatViewHolder(view, onItemClick, onDeleteClick)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ChatViewHolder(
        view: View,
        private val onItemClick: (SavedChat) -> Unit,
        private val onDeleteClick: (SavedChat) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvType: TextView = view.findViewById(R.id.tvType)
        private val tvId: TextView = view.findViewById(R.id.tvId)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        
        fun bind(chat: SavedChat) {
            tvTitle.text = chat.title
            tvType.text = chat.type.replaceFirstChar { it.uppercase() }
            tvId.text = chat.id.toString()
            
            itemView.setOnClickListener {
                onItemClick(chat)
            }
            
            btnDelete.setOnClickListener {
                onDeleteClick(chat)
            }
        }
    }
    
    class ChatDiffCallback : DiffUtil.ItemCallback<SavedChat>() {
        override fun areItemsTheSame(oldItem: SavedChat, newItem: SavedChat): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: SavedChat, newItem: SavedChat): Boolean {
            return oldItem == newItem
        }
    }
}
