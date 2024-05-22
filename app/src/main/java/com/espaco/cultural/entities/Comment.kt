package com.espaco.cultural.entities

data class Comment(val author: User, val content: String, var usersLiked: ArrayList<String>)
