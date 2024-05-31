package com.espaco.cultural.activities.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.espaco.cultural.adapters.PendingHorarioAdapter
import com.espaco.cultural.database.HorariosDB
import com.espaco.cultural.database.NotificationDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.databinding.FragmentPenddingHorarioBinding
import com.espaco.cultural.entities.Horario
import com.espaco.cultural.entities.Notification
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


class PendingHorarioFragment : Fragment() {
    private lateinit var binding: FragmentPenddingHorarioBinding
    private lateinit var context: Context
    private lateinit var adapter: PendingHorarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPenddingHorarioBinding.inflate(inflater)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Horarios solicitados" }

        context = requireContext()
        adapter = PendingHorarioAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        adapter.setOnConfirmClicked {
            confirmDialog("Aceitar horario", "Tem certeza que deseja aceitar este horario?") {
                HorariosDB.confirmHorario(it)
                adapter.removeHorario(it)
                updateListVisibility()

                Toast.makeText(context, "Horario confirmado com sucesso", Toast.LENGTH_SHORT).show()
                NotificationDB.pushNotification(it.matricula, Notification(
                    "Horario aceito",
                    "Seu horario das ${getHorarioDate(it)} foi aceito!",
                    NotificationDB.TYPE_HORARIO,
                    Date().time
                ))
            }
        }

        adapter.setOnCancelClicked {
            confirmDialog("Cancelar horario", "Tem certeza que deseja cancelar este horario?") {
                HorariosDB.removeSolicitHorario(it)
                adapter.removeHorario(it)
                updateListVisibility()

                Toast.makeText(context, "Horario cancelado com sucesso", Toast.LENGTH_SHORT).show()
                NotificationDB.pushNotification(it.matricula, Notification(
                    "Horario negado",
                    "Seu horario das ${getHorarioDate(it)} foi negado!",
                    NotificationDB.TYPE_HORARIO,
                    Date().time
                ))
            }
        }

        HorariosDB.getAllSolicitsHorarios { horarios ->
            val registrations: LinkedHashSet<String> = LinkedHashSet()
            horarios.forEach {  registrations.add(it.matricula) }

            UserDB.findUsers(registrations) { users ->
                horarios.sortWith(compareBy { it.timestamp })
                adapter.updateData(users, horarios)
                updateListVisibility()
            }
        }

        return binding.root
    }

    private fun updateListVisibility() {
        if (adapter.itemCount == 0) binding.layout.visibility = View.VISIBLE
        else binding.layout.visibility = View.GONE
    }

    private fun confirmDialog(title: String, description: String, callback: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton("Confirmar") {_, _ -> callback() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getHorarioDate(horario: Horario): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = horario.timestamp

        var hours = calendar.get(Calendar.HOUR_OF_DAY).toString()
        var minutes = calendar.get(Calendar.MINUTE).toString()

        if (hours.length < 2) hours = "0$hours"
        if (minutes.length < 2) minutes = "0$minutes"

        val format = SimpleDateFormat("dd/MM/yy")
        return "${format.format(calendar.time)} às $hours:$minutes"
    }
}