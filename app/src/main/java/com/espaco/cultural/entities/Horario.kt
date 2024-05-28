package com.espaco.cultural.entities

data class Horario(val matricula: String, val timestamp: Long, val capacity: Int, var confirmedPeople: ArrayList<String>, val public: Boolean)
