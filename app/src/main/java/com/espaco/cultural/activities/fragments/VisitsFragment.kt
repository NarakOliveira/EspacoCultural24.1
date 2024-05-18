package com.espaco.cultural.activities.fragments

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
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentVisitsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VisitsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentVisitsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

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
                    Toast.makeText(context, "Presença confirmada com sucesso na visita das ${horario}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        binding.calendarView.setTodayTextColor(ContextCompat.getColor(context, R.color.purple_700))
        binding.calendarView.setSelectedDayBackgroundColor(ContextCompat.getColor(context, R.color.purple_500))

        binding.calendarView.setOnDateSelectListener {calendar ->
            binding.textView4.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            adapter.clear();

            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            HorariosDB.getHorarios(day, month, year) {
                if (it.size > 0) {
                    binding.textView4.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
                adapter.updateData(it)
            }
        }

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VisitsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VisitsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}