package com.example.touristguide.ui.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.CategoryEntity

class CategoryChipAdapter(
    private val onClick: (CategoryEntity?) -> Unit = {}
) : RecyclerView.Adapter<CategoryChipAdapter.ViewHolder>() {
    private val items = mutableListOf<CategoryEntity>()

    fun submitList(newItems: List<CategoryEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_chip, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)

        fun bind(item: CategoryEntity) {
            titleText.text = item.name
            subtitleText.text = item.description
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
