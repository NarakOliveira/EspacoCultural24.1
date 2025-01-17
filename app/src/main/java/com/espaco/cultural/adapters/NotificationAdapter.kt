package com.espaco.cultural.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.espaco.cultural.R
import com.espaco.cultural.entities.Notification
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationHolder>() {
    private var notifications: ArrayList<Notification> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        return NotificationHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false))
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.bind(notifications[notifications.size - position - 1])
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(notifications: ArrayList<Notification>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }

    class NotificationHolder(itemView: View) : ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textContent: TextView = itemView.findViewById(R.id.textContent)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(notification: Notification) {
            textTitle.text = notification.title
            textContent.text = notification.content

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = notification.timestamp
            var hours = calendar.get(Calendar.HOUR_OF_DAY).toString()
            var minutes = calendar.get(Calendar.MINUTE).toString()

            if (hours.length < 2) hours = "0$hours"
            if (minutes.length < 2) minutes = "0$minutes"
            val format = SimpleDateFormat("dd/MM/yy")
            textDate.text = "${format.format(calendar.time)} às $hours:$minutes"
        }
    }
}