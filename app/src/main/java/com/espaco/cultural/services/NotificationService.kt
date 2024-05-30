package com.espaco.cultural.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.espaco.cultural.R
import com.espaco.cultural.database.preferences.SettingsPreferences
import com.espaco.cultural.database.preferences.UserPreferences
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationService : Service(), ValueEventListener {
    companion object {
        private const val CHANNEL_ID = "notification service"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val userPreferences = UserPreferences(this)

        if (userPreferences.isLogged()) {
            Firebase.database.reference.child("notifications")
                .child(userPreferences.registration).addValueEventListener(this)
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_ID,
                NotificationManager.IMPORTANCE_NONE
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("notification service")
                .setSmallIcon(R.drawable.ic_notifications_active)

            startForeground(1001, notification.build())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent : Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDataChange(snapshot: DataSnapshot) {
        val userPreferences = UserPreferences(this)
        val settingsPreferences = SettingsPreferences(this)

        if (!userPreferences.isLogged()) return
        if (snapshot.child("wasSeen").getValue(Boolean::class.java) != false) return

        val type = snapshot.child("type").getValue(String::class.java) ?: return
        if (type == "interaction" && !settingsPreferences.interactions) return
        if (type == "exposition" && !settingsPreferences.newExposition) return

        val title = snapshot.child("title").getValue(String::class.java) ?: ""
        val content = snapshot.child("content").getValue(String::class.java) ?: ""

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.logo_white)

        getSystemService(NotificationManager::class.java).notify(100, notification.build())
    }

    override fun onCancelled(error: DatabaseError) {
    }
}