package com.espaco.cultural.database

import com.espaco.cultural.entities.ArtWork
import com.espaco.cultural.entities.Comment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CommentsDB {
    companion object {
        fun getComments(artWork: ArtWork, callback: (comments: ArrayList<Comment>) -> Unit) {
            Firebase.database.reference
                .child("obras").child(artWork.id).child("comments").get().addOnSuccessListener {
                    val comments = ArrayList<Comment>()
                    it.children.forEach { comment ->
                        val authorRegistration = comment.child("author").getValue(String::class.java) ?: "None"
                        val content = comment.child("content").getValue(String::class.java) ?: "None"
                        val userLiked = ArrayList<String>()
                        val onceLiked = ArrayList<String>()

                        comment.child("userLiked").children.forEach { registration ->
                            userLiked.add(registration.getValue(String::class.java) ?: "")
                        }

                        comment.child("onceLiked").children.forEach { registration ->
                            onceLiked.add(registration.getValue(String::class.java) ?: "")
                        }

                        comments.add(Comment(comment.key.toString(), authorRegistration, content, userLiked, onceLiked))
                    }
                    callback(comments)
                }
        }

        fun publishComment(artWork: ArtWork, comment: Comment): Comment {
            val ref = Firebase.database.reference
                .child("obras").child(artWork.id).child("comments")

            val key: String = ref.push().key ?: ""
            ref.child(key).child("author").setValue(comment.author)
            ref.child(key).child("content").setValue(comment.content)
            ref.child(key).child("userLiked").setValue(comment.usersLiked)
            ref.child(key).child("onceLiked").setValue(comment.onceLiked)
            return Comment(key, comment.author, comment.content, comment.usersLiked, comment.onceLiked)
        }

        fun likeComment(artWork: ArtWork, comment: Comment, registration: String, status: Boolean) {
            val ref = Firebase.database.reference
                .child("obras").child(artWork.id)
                .child("comments").child(comment.id)

            if (status) {
                ref.child("userLiked").child(registration).setValue(registration)
                ref.child("onceLiked").child(registration).setValue(registration)
            } else ref.child("userLiked").child(registration).removeValue()
        }
    }
}