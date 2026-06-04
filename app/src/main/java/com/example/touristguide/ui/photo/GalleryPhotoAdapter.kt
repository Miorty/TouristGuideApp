package com.example.touristguide.ui.photo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R

class GalleryPhotoAdapter(
    private val onClick: (String) -> Unit = {}
) : RecyclerView.Adapter<GalleryPhotoAdapter.ViewHolder>() {
    private val items = mutableListOf<String>()

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_photo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)

        fun bind(path: String) {
            titleText.text = path.substringAfterLast('/')
            subtitleText.text = path
            itemView.setOnClickListener { onClick(path) }
        }
    }
}
