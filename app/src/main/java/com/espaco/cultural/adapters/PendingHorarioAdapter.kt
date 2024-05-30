package com.espaco.cultural.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.espaco.cultural.R
import com.espaco.cultural.entities.Horario
import com.espaco.cultural.entities.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class PendingHorarioAdapter : RecyclerView.Adapter<PendingHorarioAdapter.PendingHorarioHolder>() {
    private var horarios: ArrayList<Horario> = ArrayList()
    private var users: HashMap<String, User> = HashMap()

    private lateinit var onConfirmClicked: (horario: Horario) -> Unit
    private lateinit var onCancelClicked: (horario: Horario) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingHorarioHolder {
        return PendingHorarioHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pending_horario, parent, false))
    }

    override fun onBindViewHolder(holder: PendingHorarioHolder, position: Int) {
        val horario = horarios[position]
        val author = users[horario.matricula]
        holder.bind(author, horario)

        holder.buttonConfirm.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && this::onConfirmClicked.isInitialized) {
                this.onConfirmClicked(horario)
            }
        }

        holder.buttonCancel.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && this::onCancelClicked.isInitialized) {
                this.onCancelClicked(horario)
            }
        }
    }

    override fun getItemCount(): Int {
        return horarios.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(users: HashMap<String, User>, horarios: ArrayList<Horario>) {
        this.horarios = horarios;
        this.users = users;
        notifyDataSetChanged()
    }

    fun setOnConfirmClicked(onConfirmClicked: (horario : Horario) -> Unit) {
        this.onConfirmClicked = onConfirmClicked
    }

    fun setOnCancelClicked(onCancelClicked: (horario : Horario) -> Unit) {
        this.onCancelClicked = onCancelClicked
    }

    class PendingHorarioHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val textHours: TextView = itemView.findViewById(R.id.textHours)
        private val textPublic: TextView = itemView.findViewById(R.id.textPublic)
        private val textCapacity: TextView = itemView.findViewById(R.id.textCapacity)
        val buttonConfirm: ImageView = itemView.findViewById(R.id.imageConfirm)
        val buttonCancel: ImageView = itemView.findViewById(R.id.imageCancel)

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind(user: User?, horario: Horario) {
            textName.text = user?.name ?: "None"

            textCapacity.text = horario.capacity.toString()
            textPublic.text = if (horario.public) "Sim" else "Não"

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = horario.timestamp

            var hours = calendar.get(Calendar.HOUR_OF_DAY).toString()
            var minutes = calendar.get(Calendar.MINUTE).toString()

            if (hours.length < 2) hours = "0$hours"
            if (minutes.length < 2) minutes = "0$minutes"

            val format = SimpleDateFormat("dd/MM/yy")
            textDate.text = format.format(calendar.time)
            textHours.text = "$hours:$minutes"
        }
    }
}