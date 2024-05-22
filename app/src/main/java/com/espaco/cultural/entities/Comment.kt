package com.espaco.cultural.entities

data class Comment(val author: String, val content: String, var usersLiked: ArrayList<String>)
