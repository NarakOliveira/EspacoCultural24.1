package com.espaco.cultural.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.espaco.cultural.R
import com.espaco.cultural.entities.Horario
import com.espaco.cultural.entities.User
import java.util.Calendar
import java.util.TimeZone
import java.util.logging.Handler
import kotlin.math.sign

class HorarioAdapter : RecyclerView.Adapter<HorarioAdapter.HorarioHolder>() {
    private var horarios:  ArrayList<Horario> = ArrayList()
    private var users: HashMap<String, User> = HashMap()
    private lateinit var onHorarioClicked: (horario: Horario) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioHolder {
        return HorarioHolder(LayoutInflater.from(parent.context).inflate(R.layout.horarios, parent, false))
    }

    override fun onBindViewHolder(holder: HorarioHolder, position: Int) {
        val horario = this.horarios[position]
        holder.bind(horario)

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.getAdapterPosition()
            if (adapterPosition != RecyclerView.NO_POSITION && this::onHorarioClicked.isInitialized) {
                this.onHorarioClicked(horario)
            }
        }
    }

    override fun getItemCount(): Int {
        return horarios.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(users: HashMap<String, User>, horarios: ArrayList<Horario>) {
        this.horarios = horarios
        this.users = users
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        this.horarios.clear()
        notifyDataSetChanged()
    }

    fun addPeople(oldHorario: Horario, matricula: String) {
        val index = horarios.indexOf(oldHorario)
        if (index == -1) return

        this.horarios[index].confirmedPeople.add(matricula)
        this.notifyItemChanged(index)
    }

    fun setOnHorarioClicked(onHorarioClicked: (horario: Horario) -> Unit) {
       this.onHorarioClicked = onHorarioClicked;
    }

    class HorarioHolder(itemView: View) : ViewHolder(itemView) {
        private val textHorario: TextView = itemView.findViewById(R.id.textView3)
        private val progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
        private val originalFlags = textHorario.paintFlags

        @SuppressLint("SetTextI18n")
        fun bind(horario: Horario) {
            if (horario.capacity <= horario.confirmedPeople.size) textHorario.paintFlags = textHorario.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            else textHorario.paintFlags = originalFlags

            progressBar.max = horario.capacity
            progressBar.progress = horario.confirmedPeople.size

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = horario.timestamp
            textHorario.text = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
        }
    }
}