package com.senseigram.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.data.MessageDraft
import com.senseigram.databinding.ItemHomeDraftBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeDraftAdapter(
    private val onItemClick: (MessageDraft) -> Unit
) : ListAdapter<MessageDraft, HomeDraftAdapter.VH>(Diff) {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    inner class VH(val binding: ItemHomeDraftBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageDraft) {
            val ctx = binding.root.context
            binding.draftPreview.text = item.text.ifEmpty { ctx.getString(R.string.empty_message) }
            binding.draftTime.text = timeFormat.format(Date(item.timestamp))
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHomeDraftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
