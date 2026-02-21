package com.senseigram.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.data.SavedChat

class ChatsAdapter(
    private val onClick: (SavedChat) -> Unit,
    private val onDelete: (SavedChat) -> Unit
) : ListAdapter<SavedChat, ChatsAdapter.VH>(Diff()) {
    
    class VH(view: View, private val onClick: (SavedChat) -> Unit, private val onDelete: (SavedChat) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(chat: SavedChat) {
            itemView.findViewById<TextView>(R.id.tvTitle).text = chat.title
            itemView.findViewById<TextView>(R.id.tvType).text = chat.type.uppercase()
            itemView.setOnClickListener { onClick(chat) }
            itemView.findViewById<View>(R.id.btnDelete).setOnClickListener { onDelete(chat) }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false), onClick, onDelete
    )
    
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
    
    class Diff : DiffUtil.ItemCallback<SavedChat>() {
        override fun areItemsTheSame(a: SavedChat, b: SavedChat) = a.id == b.id
        override fun areContentsTheSame(a: SavedChat, b: SavedChat) = a == b
    }
}
