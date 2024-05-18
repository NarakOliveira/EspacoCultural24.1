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
                    val confirmedPeoples = child.child("confirmedPeoples").getValue(Int::class.java) ?: 0
                    val horario = Horario(child.key!!.toLong(), capacity, arrayListOf())
                    for (i in 0..confirmedPeoples) horario.confirmedPeople.add("")

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
    }
}