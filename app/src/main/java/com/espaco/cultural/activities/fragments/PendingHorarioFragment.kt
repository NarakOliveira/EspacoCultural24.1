package com.espaco.cultural.activities.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.adapters.PendingHorarioAdapter
import com.espaco.cultural.database.HorariosDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.databinding.FragmentPenddingHorarioBinding


class PendingHorarioFragment : Fragment() {
    private lateinit var binding: FragmentPenddingHorarioBinding
    private lateinit var context: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPenddingHorarioBinding.inflate(inflater)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Horarios solicitados" }

        context = requireContext()
        val adapter = PendingHorarioAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        adapter.setOnConfirmClicked {
            confirmDialog("Aceitar horario", "Tem certeza que deseja aceitar este horario?") {
            }
        }

        adapter.setOnCancelClicked {
            confirmDialog("Cancelar horario", "Tem certeza que deseja cancelar este horario?") {
            }
        }

        HorariosDB.getAllSolicitsHorarios { horarios ->
            val registrations: LinkedHashSet<String> = LinkedHashSet()
            horarios.forEach {  registrations.add(it.matricula) }

            UserDB.findUsers(registrations) { users ->
                horarios.sortWith(compareBy { it.timestamp })
                if (horarios.isEmpty()) binding.layout.visibility = View.VISIBLE
                else binding.layout.visibility = View.GONE
                adapter.updateData(users, horarios)
            }
        }

        return binding.root
    }

    fun confirmDialog(title: String, description: String, callback: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton("Confirmar") {_, _ -> callback() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}