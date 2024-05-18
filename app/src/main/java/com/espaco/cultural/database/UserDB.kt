package com.espaco.cultural.database

import com.espaco.cultural.database.exceptions.DatabaseException
import com.espaco.cultural.database.exceptions.InvalidPasswordException
import com.espaco.cultural.database.exceptions.UserNotFoundException
import com.espaco.cultural.entities.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class UserDB {
    companion object {
        private val usersReference: DatabaseReference = Firebase.database.reference.child("users")

        fun login(registration: String, password: String, onFinish: (user: User?, error: DatabaseException?) -> Unit) {
           usersReference.child(registration).get().addOnSuccessListener {
               if (!it.exists()) {
                   onFinish(null, UserNotFoundException())
                   return@addOnSuccessListener
               }

               val validPassword = it.child("password").getValue(String::class.java)
               if (password != validPassword) {
                   onFinish(null, InvalidPasswordException())
                   return@addOnSuccessListener
               }

               val userName = it.child("name").getValue(String::class.java) as String
               val userPicture = it.child("picture").getValue(String::class.java) as String
               val userIsAdmin = it.child("isAdmin").getValue(Boolean::class.java) as Boolean
               onFinish(User(registration, userName, userPicture, userIsAdmin), null)
           }.addOnFailureListener {
               onFinish(null, DatabaseException())
           }
        }

        fun findUser(registration: String, callback: (user: User) -> Unit) {
            usersReference.child(registration).get().addOnSuccessListener {
                if (!it.exists()) return@addOnSuccessListener
                val userName = it.child("name").getValue(String::class.java) as String
                val userPicture = it.child("picture").getValue(String::class.java) as String
                val userIsAdmin = it.child("isAdmin").getValue(Boolean::class.java) as Boolean
                callback(User(registration, userName, userPicture, userIsAdmin))
            }
        }
    }
}