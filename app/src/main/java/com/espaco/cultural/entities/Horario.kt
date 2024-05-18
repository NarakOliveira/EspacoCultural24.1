package com.espaco.cultural.entities

data class Horario(val timestamp: Long, val capacity: Int, var confirmedPeople: ArrayList<String>)
