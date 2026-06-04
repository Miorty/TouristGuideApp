package com.example.touristguide.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.ActivityLogEntity

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    private val items = mutableListOf<ActivityLogEntity>()

    fun submitList(newItems: List<ActivityLogEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_post, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)
        private val feedImage = itemView.findViewById<ImageView>(R.id.feedImage)
        private val likesText = itemView.findViewById<TextView>(R.id.likesText)
        private val dateText = itemView.findViewById<TextView>(R.id.dateText)
        private val viewsText = itemView.findViewById<TextView>(R.id.viewsText)
        private val relatedPlaceContainer = itemView.findViewById<View>(R.id.relatedPlaceContainer)
        private val feedDivider = itemView.findViewById<View>(R.id.feedDivider)

        @Suppress("UNUSED_PARAMETER")
        fun bind(item: ActivityLogEntity) {
            val second = bindingAdapterPosition % 2 == 1
            titleText.text = if (second) "Петр Петров" else "Иван Иванов"
            feedImage.setImageResource(if (second) R.drawable.feed_gate else R.drawable.feed_stone)
            subtitleText.text = if (second) "Пермь - потрясающий промышленный город." else "Необычная история появления!"
            likesText.text = if (second) "3" else "40"
            dateText.text = if (second) "1 января 2026 г." else "5 мая 2026 г."
            viewsText.text = if (second) "91" else "2091"
            relatedPlaceContainer.visibility = if (second) View.GONE else View.VISIBLE
            feedDivider.visibility = if (second) View.GONE else View.VISIBLE
        }
    }
}
