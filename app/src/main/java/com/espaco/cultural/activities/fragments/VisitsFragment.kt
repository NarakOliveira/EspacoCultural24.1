package com.espaco.cultural.activities.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.adapters.HorarioAdapter
import com.espaco.cultural.database.HorariosDB
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentVisitsBinding
import com.espaco.cultural.entities.Horario
import com.espaco.cultural.entities.User
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker


class VisitsFragment : Fragment() {
    private lateinit var binding: FragmentVisitsBinding

    private lateinit var context: Context
    private lateinit var userPreferences: UserPreferences
    private lateinit var adapter: HorarioAdapter

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVisitsBinding.inflate(inflater)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Visitas" }

        context = requireContext()
        userPreferences = UserPreferences(context)
        adapter = HorarioAdapter()

        binding.floatingActionButton.visibility = View.VISIBLE
        binding.floatingActionButton.setOnClickListener { addHorario() }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(context, 5)

        adapter.setOnHorarioClicked {
            if (userPreferences.isAdmin) {
                val ldf = VisitorsListFragment()
                val args = Bundle()
                ldf.setArguments(args)
                args.putString("matricula", it.matricula)
                args.putLong("timestamp", it.timestamp)
                args.putInt("capacity", it.capacity)
                args.putStringArrayList("confirmedPeople", it.confirmedPeople)
                args.putBoolean("public", it.public)

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragmentContainer, ldf)
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
        updateAdapter(binding.calendarView.selectedDayCalendar)

        binding.calendarView.isDisableAllDates = true
        binding.calendarView.dayTextColor = Color.BLACK
        HorariosDB.getAllHorariosForUser(User(
            userPreferences.registration,
                userPreferences.name,
                userPreferences.picture,
                userPreferences.isAdmin
        )) { dates ->
            dates.forEach {
                binding.calendarView.enableDate(it)
            }
            binding.calendarView.redraw()
        }

        binding.calendarView.setOnDateSelectListener {calendar ->
            updateAdapter(calendar)
        }


        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateAdapter(calendar: java.util.Calendar) {
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

            var i = horarios.size - 1
            while (i >= 0) {
                val it = horarios[i]
                println(it.matricula + " " + it.public)
                if (!userPreferences.isAdmin && !it.public && it.matricula != userPreferences.registration) {
                    horarios.removeAt(i)
                }
                i--
            }

            adapter.updateData(horarios)
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun addHorario() {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val paddingDp = 8f
        val paddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            paddingDp,
            context.resources.displayMetrics
        ).toInt()
        layout.updatePadding(paddingPx, paddingPx, paddingPx)

        val switch = MaterialSwitch(context)
        switch.text = "Visitação publica"
        switch.isChecked = true
        layout.addView(switch)

        val editText = EditText(context)
        editText.hint = "Número de pessoas"
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(editText)

        editText.addTextChangedListener {
            if (it == null || it.isEmpty()) return@addTextChangedListener
            if (Integer.parseInt(it.toString()) <= 0) editText.setText("1")
            else if (Integer.parseInt(it.toString()) > 30) editText.setText("30")
        }

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true

        val progressDialog = AlertDialog.Builder(context)
            .setView(progressBar)
            .setCancelable(false)
            .create()

        progressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val picker = MaterialTimePicker.Builder()
            .setTitleText("Selecionar horario")
            .setPositiveButtonText("Confirmar")
            .setNegativeButtonText("Cancelar")
            .setHour(binding.calendarView.selectedDayCalendar.get(Calendar.HOUR))
            .setMinute(binding.calendarView.selectedDayCalendar.get(Calendar.MINUTE))
            .build()

        val positiveButtonText = if (userPreferences.isAdmin) "Agendar" else "Solicitar"

        val dialog = AlertDialog.Builder(context)
            .setView(layout)
            .setPositiveButton(positiveButtonText) { _, _ ->
                val c = binding.calendarView.selectedDayCalendar;
                c.set(Calendar.HOUR_OF_DAY, picker.hour)
                c.set(Calendar.MINUTE, picker.minute)
                var capacity = editText.text.toString()
                if (capacity.isEmpty()) capacity = "30"
                val horario = Horario(userPreferences.registration, c.timeInMillis, Integer.valueOf(capacity), arrayListOf(), switch.isChecked)
                if (userPreferences.isAdmin) {
                    HorariosDB.publishHorario(horario)
                    adapter.addHorario(horario)
                } else {
                    HorariosDB.solicitHorario(horario)
                    Toast.makeText(context, "Solicitação de agendamento enviada para os organizadores", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        picker.addOnPositiveButtonClickListener {
            progressDialog.show()
            val c = binding.calendarView.selectedDayCalendar;
            c.set(Calendar.HOUR_OF_DAY, picker.hour)
            c.set(Calendar.MINUTE, picker.minute)

            HorariosDB.hasConflictInHorario(c) {
                progressDialog.dismiss()
                if (!it) {
                    var hours = picker.hour.toString()
                    var minutes = picker.minute.toString()
                    if (hours.length < 2) hours = "0$hours"
                    if (minutes.length < 2) minutes = "0$minutes"

                    if (userPreferences.isAdmin) dialog.setTitle("Agendar horario as $hours:$minutes")
                    else dialog.setTitle("Solicitar horario as $hours:$minutes")
                    dialog.show()
                } else {
                    Toast.makeText(context, "Existe um horario que esta em conflito com esse", Toast.LENGTH_SHORT).show()
                }
            }
        }

        picker.show(parentFragmentManager, "")
    }
}