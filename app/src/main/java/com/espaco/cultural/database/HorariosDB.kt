package com.espaco.cultural.database

import com.espaco.cultural.entities.Horario
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.TimeZone

class HorariosDB {
    companion object {
        private val horariosReference: DatabaseReference = Firebase.database.reference.child("horarios")

        fun getHorarios(day: Int,  month: Int,  year: Int, callback: (horarios: ArrayList<Horario>) -> Unit) {
            horariosReference.get().addOnSuccessListener {
                val horarios = ArrayList<Horario>()
                it.children.forEach { child ->
                    val capacity = child.child("capacity").getValue(Int::class.java) ?: 0
                    val horario = Horario(child.key!!.toLong(), capacity, arrayListOf())

                    val confirmedPeoples = child.child("visitors").children
                    confirmedPeoples.forEach { registration ->
                        horario.confirmedPeople.add(registration.getValue(String::class.java) ?: "")
                    }

                   // for (i in 0..confirmedPeoples) horario.confirmedPeople.add("")

                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.timeInMillis = horario.timestamp

                    if (calendar.get(Calendar.DAY_OF_MONTH) != day) return@forEach
                    if (calendar.get(Calendar.MONTH) != month) return@forEach
                    if (calendar.get(Calendar.YEAR) != year) return@forEach

                    horarios.add(horario);
                }

                callback(horarios)
            }
        }

        fun getAllHorarios(callback: (horarios: ArrayList<Calendar>) -> Unit) {
            horariosReference.get().addOnSuccessListener {
                val horarios = ArrayList<Calendar>()
                it.children.forEach { child ->
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.timeInMillis = child.key!!.toLong()
                    horarios.add(calendar)
                }
                callback(horarios)
            }
        }

        fun confirmUser(horario: Horario, registration: String, callback: (status: Boolean) -> Unit) {
            val ref = horariosReference.child(horario.timestamp.toString())
            ref.get().addOnSuccessListener {
                val capacity = it.child("capacity").getValue(Int::class.java) ?: 0
                var confirmedPeoples = it.child("confirmedPeoples").getValue(Int::class.java) ?: 0
                if (capacity <= confirmedPeoples) {
                    callback(false)
                    return@addOnSuccessListener
                }

                confirmedPeoples++
                ref.child("confirmedPeoples").setValue(confirmedPeoples)
                ref.child("visitors").child(ref.push().key.toString()).setValue(registration)

                callback(true)
            }.addOnFailureListener { callback(false) }
        }
    }
}