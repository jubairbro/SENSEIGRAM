package com.senseigram.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.data.InlineBtn
import com.senseigram.databinding.ItemButtonRowBinding

class ButtonRowAdapter(
    private val onRemoveRow: (Int) -> Unit
) : RecyclerView.Adapter<ButtonRowAdapter.VH>() {

    private val rows = mutableListOf<List<InlineBtn>>()

    inner class VH(val binding: ItemButtonRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: List<InlineBtn>) {
            binding.buttonRowContainer.removeAllViews()
            val ctx = binding.root.context

            row.forEach { btn ->
                val tv = TextView(ctx).apply {
                    text = btn.text.ifEmpty { "Button" }
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    setPadding(24, 12, 24, 12)
                    setBackgroundColor(
                        when (btn.style) {
                            1 -> Color.parseColor("#3B82F6")
                            2 -> Color.parseColor("#22C55E")
                            3 -> Color.parseColor("#EF4444")
                            4 -> Color.parseColor("#F59E0B")
                            else -> Color.parseColor("#374151")
                        }
                    )
                    val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    lp.marginEnd = 4
                    layoutParams = lp
                    gravity = android.view.Gravity.CENTER
                }
                binding.buttonRowContainer.addView(tv)
            }

            binding.removeRowBtn.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onRemoveRow(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemButtonRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount() = rows.size

    fun setRows(newRows: List<List<InlineBtn>>) {
        rows.clear()
        rows.addAll(newRows)
        notifyDataSetChanged()
    }

    fun addButtonToLastRow(btn: InlineBtn) {
        if (rows.isEmpty()) {
            rows.add(listOf(btn))
        } else {
            val lastRow = rows.last().toMutableList()
            lastRow.add(btn)
            rows[rows.size - 1] = lastRow
        }
        notifyDataSetChanged()
    }

    fun addButtonToNewRow(btn: InlineBtn) {
        rows.add(listOf(btn))
        notifyItemInserted(rows.size - 1)
    }

    fun removeRow(position: Int) {
        if (position in rows.indices) {
            rows.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, rows.size - position)
        }
    }

    fun getRows(): List<List<InlineBtn>> = rows.toList()

    fun clear() {
        rows.clear()
        notifyDataSetChanged()
    }
}
