package com.example.touristguide.ui.place_detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.PlacePhotoEntity

class PlacePhotoAdapter : RecyclerView.Adapter<PlacePhotoAdapter.ViewHolder>() {
    private val items = mutableListOf<PlacePhotoEntity>()

    fun submitList(newItems: List<PlacePhotoEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place_photo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)

        fun bind(item: PlacePhotoEntity) {
            titleText.text = "Фото #${item.id}"
            subtitleText.text = item.filePath
        }
    }
}
