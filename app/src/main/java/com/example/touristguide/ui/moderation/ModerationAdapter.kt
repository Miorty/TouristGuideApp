package com.example.touristguide.ui.moderation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.touristguide.R
import com.example.touristguide.data.local.entity.ModerationQueueEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ModerationAdapter(
    private val onApprove: (ModerationQueueEntity, String) -> Unit,
    private val onReject: (ModerationQueueEntity, String) -> Unit
) : RecyclerView.Adapter<ModerationAdapter.ViewHolder>() {
    private val items = mutableListOf<ModerationQueueEntity>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    fun submitList(newItems: List<ModerationQueueEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_moderation, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText = itemView.findViewById<TextView>(R.id.titleText)
        private val subtitleText = itemView.findViewById<TextView>(R.id.subtitleText)
        private val statusText = itemView.findViewById<TextView>(R.id.statusText)
        private val commentEditText = itemView.findViewById<EditText>(R.id.commentEditText)
        private val approveButton = itemView.findViewById<Button>(R.id.approveButton)
        private val rejectButton = itemView.findViewById<Button>(R.id.rejectButton)

        fun bind(item: ModerationQueueEntity) {
            titleText.text = when (item.entityType) {
                "PLACE" -> "Новая точка маршрута"
                "REVIEW" -> "Новый отзыв"
                "PHOTO" -> "Новое фото"
                "REPORT" -> "Жалоба пользователя"
                else -> "Материал #${item.entityId}"
            }
            subtitleText.text = "${item.entityType} • Иван Иванов • ${dateFormat.format(Date(item.createdAt))}"
            statusText.text = item.status
            commentEditText.setText(item.comment)
            approveButton.setOnClickListener {
                onApprove(item, commentEditText.text.toString())
            }
            rejectButton.setOnClickListener {
                onReject(item, commentEditText.text.toString())
            }
        }
    }
}
