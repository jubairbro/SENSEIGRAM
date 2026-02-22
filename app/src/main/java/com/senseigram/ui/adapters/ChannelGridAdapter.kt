package com.senseigram.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.data.SavedChat
import com.senseigram.databinding.ItemChannelGridBinding

class ChannelGridAdapter(
    private val onItemClick: (SavedChat) -> Unit
) : ListAdapter<SavedChat, ChannelGridAdapter.VH>(Diff) {

    inner class VH(val binding: ItemChannelGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SavedChat) {
            binding.channelTitle.text = item.title
            binding.channelId.text = item.id.toString()
            binding.channelInitial.text = item.title.firstOrNull()?.uppercase() ?: "?"
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemChannelGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
