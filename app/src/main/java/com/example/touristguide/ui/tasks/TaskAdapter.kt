package com.example.touristguide.ui.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.TaskEntity

class TaskAdapter(
    private val onClick: (TaskEntity) -> Unit = {}
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    private val items = mutableListOf<TaskEntity>()

    fun submitList(newItems: List<TaskEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)

        fun bind(item: TaskEntity) {
            titleText.text = item.title
            subtitleText.text = "${item.description} • цель ${item.targetValue}, награда ${item.pointsReward}"
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
