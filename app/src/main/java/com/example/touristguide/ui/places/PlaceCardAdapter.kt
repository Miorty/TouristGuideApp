package com.example.touristguide.ui.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.PlaceEntity

class PlaceCardAdapter(
    private val onClick: (PlaceEntity) -> Unit = {}
) : RecyclerView.Adapter<PlaceCardAdapter.ViewHolder>() {
    private val items = mutableListOf<PlaceEntity>()

    fun submitList(newItems: List<PlaceEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeImage = itemView.findViewById<ImageView>(R.id.placeImage)
        private val categoryChip = itemView.findViewById<TextView>(R.id.categoryChip)
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)
        private val favoriteIcon = itemView.findViewById<ImageView>(R.id.favoriteIcon)

        fun bind(item: PlaceEntity) {
            val position = bindingAdapterPosition
            placeImage.setImageResource(imageFor(item.title))
            categoryChip.text = "Памятники и скульптуры"
            titleText.text = item.title
            subtitleText.text = item.address.ifBlank { "г. Пермь" }
            favoriteIcon.setImageResource(if (position == 0) R.drawable.ic_favorite_filled else R.drawable.ic_favorite)
            itemView.setOnClickListener { onClick(item) }
        }

        private fun imageFor(title: String): Int = when {
            title.contains("медвед", ignoreCase = true) -> R.drawable.place_bear
            title.contains("солен", ignoreCase = true) || title.contains("уш", ignoreCase = true) -> R.drawable.place_ears
            title.contains("галере", ignoreCase = true) || title.contains("эспланад", ignoreCase = true) -> R.drawable.place_art_object
            else -> R.drawable.place_bear
        }
    }
}
