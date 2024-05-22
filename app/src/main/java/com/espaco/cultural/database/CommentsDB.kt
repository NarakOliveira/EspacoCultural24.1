package com.espaco.cultural.database

import com.espaco.cultural.entities.ArtWork
import com.espaco.cultural.entities.Comment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CommentsDB {
    companion object {
        fun getComments(artWork: ArtWork, callback: (comments: ArrayList<Comment>) -> Unit) {
            Firebase.database.reference
                .child("obras").child(artWork.id).child("comments")
                .get().addOnSuccessListener {
                    val comments = ArrayList<Comment>()
                    it.children.forEach { comment ->
                        val authorRegistration = comment.child("author").getValue(String::class.java) ?: "None"
                        val content = comment.child("content").getValue(String::class.java) ?: "None"
                        val userLiked = ArrayList<String>()

                        comment.child("userLiked").children.forEach { registration ->
                            userLiked.add(registration.getValue(String::class.java) ?: "")
                        }

                        comments.add(Comment(authorRegistration, content, userLiked))
                    }
                    callback(comments)
                }
        }
    }
}