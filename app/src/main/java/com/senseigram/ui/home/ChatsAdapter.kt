package com.senseigram.ui.home

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

class ChatsAdapter(
    private val onClick: (SavedChat) -> Unit,
    private val onDelete: (SavedChat) -> Unit
) : ListAdapter<SavedChat, ChatsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view, onClick, onDelete)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        view: View,
        private val onClick: (SavedChat) -> Unit,
        private val onDelete: (SavedChat) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvType: TextView = view.findViewById(R.id.tvType)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        
        fun bind(chat: SavedChat) {
            tvTitle.text = chat.title
            tvType.text = chat.type
            itemView.setOnClickListener { onClick(chat) }
            btnDelete.setOnClickListener { onDelete(chat) }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<SavedChat>() {
        override fun areItemsTheSame(old: SavedChat, new: SavedChat) = old.id == new.id
        override fun areContentsTheSame(old: SavedChat, new: SavedChat) = old == new
    }
}
