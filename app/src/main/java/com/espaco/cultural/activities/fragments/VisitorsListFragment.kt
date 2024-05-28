package com.espaco.cultural.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.adapters.VisitorsAdapter
import com.espaco.cultural.database.HorariosDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.databinding.FragmentVisitorsListBinding
import com.espaco.cultural.entities.Horario


class VisitorsListFragment : Fragment() {
    private lateinit var binding: FragmentVisitorsListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = requireArguments()
        val horario = Horario(
            args.getString("matricula") ?: "",
            args.getLong("timestamp") ?: -1,
            args.getInt("capacity") ?: -1,
            args.getStringArrayList("confirmedPeople") ?: ArrayList(),
            args.getBoolean("public")
        )

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Visitantes" }

        binding = FragmentVisitorsListBinding.inflate(inflater)

        val adapter = VisitorsAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        HorariosDB.getVisitors(horario) {
            UserDB.findUsers(it) {users ->
                binding.linear.visibility = View.GONE
                adapter.updateData(ArrayList(users.values))
            }
        }

        return binding.root
    }
}