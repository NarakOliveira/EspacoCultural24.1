package com.espaco.cultural.activities.fragments

import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.adapters.HorarioAdapter
import com.espaco.cultural.database.HorariosDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentVisitsBinding


class VisitsFragment : Fragment() {
    private lateinit var binding: FragmentVisitsBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisitsBinding.inflate(inflater)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Visitas" }

        val context = requireContext()
        val userPreferences = UserPreferences(context)
        val adapter = HorarioAdapter()

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(context, 5)


        adapter.setOnHorarioClicked {
            if (userPreferences.isAdmin) {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragmentContainer, VisitorsListFragment())
                    .commit()
                return@setOnHorarioClicked
            }

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = it.timestamp
            val horario  = "${calendar.get(java.util.Calendar.HOUR_OF_DAY)}:${calendar.get(java.util.Calendar.MINUTE)}"

            if (it.capacity <= it.confirmedPeople.size) {
                Toast.makeText(context, "Todas as vagas para este horario já foram preenchidas", Toast.LENGTH_LONG).show()
                return@setOnHorarioClicked
            }

            if (it.confirmedPeople.indexOf(userPreferences.registration) != -1) {
                Toast.makeText(context, "Você ja confirmou sua presença nesta visita", Toast.LENGTH_LONG).show()
                return@setOnHorarioClicked
            }

            AlertDialog.Builder(context)
                .setTitle("Confirmar presença")
                .setMessage("Deseja confirmar sua presença na visita das ${horario}?")
                .setPositiveButton("Confirmar") { _, _ ->
                    adapter.addPeople(it, userPreferences.registration)
                    HorariosDB.confirmUser(it, userPreferences.registration) { status ->
                        if (status) Toast.makeText(context, "Presença confirmada com sucesso na visita das $horario", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(context, "Erro ao tenta confirmar a presença", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.calendarView.setTodayTextColor(ContextCompat.getColor(context, R.color.purple_700))
        binding.calendarView.setSelectedDayBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))

        binding.calendarView.isDisableAllDates = true
        binding.calendarView.dayTextColor = Color.BLACK
        HorariosDB.getAllHorarios {dates ->
            dates.forEach { binding.calendarView.enableDate(it) }
            binding.calendarView.redraw()
        }

        binding.calendarView.setOnDateSelectListener {calendar ->
            binding.textView4.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            adapter.clear();

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            HorariosDB.getHorarios(day, month, year) { horarios ->
                if (horarios.size > 0) {
                    binding.textView4.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
                val registrations: LinkedHashSet<String> = LinkedHashSet()
                horarios.forEach { registrations.add(it.matricula) }
                UserDB.findUsers(registrations) {
                    adapter.updateData(it, horarios)
                }
            }
        }

        return binding.root
    }

}