package com.espaco.cultural.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.espaco.cultural.R
import com.espaco.cultural.databinding.FragmentPenddingHorarioBinding


class PendingHorarioFragment : Fragment() {
    private lateinit var binding: FragmentPenddingHorarioBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPenddingHorarioBinding.inflate(inflater)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Horarios solicitados" }

        return binding.root
    }
}