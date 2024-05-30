package com.espaco.cultural.entities

data class Notification(val title: String, val content: String, val type: String, val timestamp: Long, var wasSeen: Boolean = false)
