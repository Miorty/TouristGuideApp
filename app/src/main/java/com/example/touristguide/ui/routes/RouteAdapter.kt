package com.example.touristguide.ui.routes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.RouteEntity

class RouteAdapter(
    private val onClick: (RouteEntity) -> Unit = {}
) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {
    private val items = mutableListOf<RouteEntity>()

    fun submitList(newItems: List<RouteEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)

        fun bind(item: RouteEntity) {
            titleText.text = item.title
            subtitleText.text = when {
                item.title.contains("Зелен", ignoreCase = true) -> "37 достопримечательностей"
                item.title.contains("Крас", ignoreCase = true) -> "16 достопримечательностей"
                else -> "31 достопримечательность"
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
