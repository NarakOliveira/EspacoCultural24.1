package com.espaco.cultural.database

import com.espaco.cultural.entities.ArtWork
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ArtWorkDB {
    companion object {
        private val artWorkReference: DatabaseReference = Firebase.database.reference.child("obras")

        fun getArtWorks(callback: (artWorks: ArrayList<ArtWork>) -> Unit) {
            artWorkReference.get().addOnSuccessListener {
                val artWorks = ArrayList<ArtWork>()
                it.children.forEach { child ->
                    val title = child.child("title").getValue(String::class.java) ?: "None"
                    val autor = child.child("autor").getValue(String::class.java) ?: "None"
                    val description = child.child("description").getValue(String::class.java) ?: "None"
                    val image = child.child("image").getValue(String::class.java) ?: ""
                    artWorks.add(ArtWork(child.key ?: "None", title, autor, description, image))
                }
                callback(artWorks)
            }
        }

        fun publishArtWork(artWork: ArtWork) {
            val key: String = artWorkReference.push().getKey() ?: ""
            artWorkReference.child(key).child("title").setValue(artWork.title)
            artWorkReference.child(key).child("autor").setValue(artWork.autor)
            artWorkReference.child(key).child("description").setValue(artWork.description)
            artWorkReference.child(key).child("image").setValue(artWork.image)
        }

        fun findArtWork(id: String, callback: (artWork: ArtWork?) -> Unit) {
            artWorkReference.child(id).get().addOnSuccessListener { child ->
                val title = child.child("title").getValue(String::class.java)
                val autor = child.child("autor").getValue(String::class.java)
                val description = child.child("description").getValue(String::class.java)
                val image = child.child("image").getValue(String::class.java)
                if (title == null || autor == null || description == null || image == null) {
                    callback(null)
                } else callback(ArtWork(id, title, autor, description, image))
            }.addOnFailureListener {
                callback(null)
            }
        }
    }
}