package com.senseigram.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.data.InlineBtn
import com.senseigram.databinding.ItemInlineButtonBinding

class ButtonsAdapter : RecyclerView.Adapter<ButtonsAdapter.VH>() {
    private val items = mutableListOf<ButtonItem>()
    
    data class ButtonItem(
        var text: String = "",
        var url: String = "",
        var style: Int = 0
    )
    
    inner class VH(val binding: ItemInlineButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ButtonItem) {
            binding.buttonTextInput.setText(item.text)
            binding.buttonUrlInput.setText(item.url)
            
            binding.buttonTextInput.setOnFocusChangeListener { _, _ ->
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos].text = binding.buttonTextInput.text.toString()
                }
            }
            binding.buttonUrlInput.setOnFocusChangeListener { _, _ ->
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos].url = binding.buttonUrlInput.text.toString()
                }
            }
            
            binding.removeButton.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, items.size - pos)
                }
            }
            
            setupColorPicker(item)
            updatePreviewButton(item)
        }
        
        private fun setupColorPicker(item: ButtonItem) {
            val colorButtons = listOf(
                binding.colorDefault to 0,
                binding.colorPrimary to 1,
                binding.colorSuccess to 2,
                binding.colorDanger to 3,
                binding.colorWarning to 4
            )
            
            colorButtons.forEach { (btn, style) ->
                btn.isSelected = item.style == style
                btn.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        items[pos].style = style
                        colorButtons.forEach { (b, _) -> b.isSelected = false }
                        btn.isSelected = true
                        updatePreviewButton(items[pos])
                    }
                }
            }
        }
        
        private fun updatePreviewButton(item: ButtonItem) {
            binding.previewButton.text = item.text.ifEmpty { "Button" }
            val color = when (item.style) {
                1 -> Color.parseColor("#3B82F6")
                2 -> Color.parseColor("#22C55E")
                3 -> Color.parseColor("#EF4444")
                4 -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#374151")
            }
            binding.previewButton.setBackgroundColor(color)
            binding.previewButton.setTextColor(Color.WHITE)
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemInlineButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }
    
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount() = items.size
    
    fun addButton() {
        items.add(ButtonItem())
        notifyItemInserted(items.size - 1)
    }
    
    fun getButtons(): List<List<InlineBtn>> {
        return items.filter { it.text.isNotEmpty() }.map { btn ->
            listOf(InlineBtn(btn.text, btn.url.ifEmpty { null }, null, btn.style))
        }
    }
}
