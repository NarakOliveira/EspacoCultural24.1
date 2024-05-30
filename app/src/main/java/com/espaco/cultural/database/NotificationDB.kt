package com.espaco.cultural.database

import com.espaco.cultural.entities.Notification
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationDB {
    companion object {
        const val TYPE_INTERACTION = "interaction"
        const val TYPE_EXPOSITION = "exposition"

        private val notificationReference: DatabaseReference = Firebase.database.reference.child("notifications")

        fun markAsWasSeen(userRegistration: String, keys: ArrayList<String>) {
            val ref = notificationReference.child(userRegistration)
            keys.forEach { ref.child(it).child("wasSeen").setValue(true) }
        }

        fun pushNotification(registration: String, notification: Notification) {
            val ref = notificationReference.child(registration)
            val key: String = ref.push().getKey() ?: ""

            ref.child(key).child("title").setValue(notification.title)
            ref.child(key).child("content").setValue(notification.content)
            ref.child(key).child("type").setValue(notification.type)
            ref.child(key).child("wasSeen").setValue(notification.wasSeen)
        }
        
        fun broadcastNotification(registrations: ArrayList<String>, notification: Notification) {
            registrations.forEach { pushNotification(it, notification) }
        }
    }
}