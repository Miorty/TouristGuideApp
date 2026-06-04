package com.example.touristguide.ui.routes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.PlaceEntity

class RoutePlaceAdapter : RecyclerView.Adapter<RoutePlaceAdapter.ViewHolder>() {
    private val items = mutableListOf<PlaceEntity>()

    fun submitList(newItems: List<PlaceEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route_place, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position + 1, items[position])

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)

        fun bind(position: Int, item: PlaceEntity) {
            titleText.text = "$position. ${item.title}"
            subtitleText.text = item.address
        }
    }
}
