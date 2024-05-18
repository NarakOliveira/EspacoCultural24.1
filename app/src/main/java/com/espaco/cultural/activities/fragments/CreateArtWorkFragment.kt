package com.espaco.cultural.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.espaco.cultural.R
import com.espaco.cultural.databinding.FragmentCreateArtWorkBinding

class CreateArtWorkFragment : Fragment() {
    private lateinit var binding: FragmentCreateArtWorkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateArtWorkBinding.inflate(inflater)
        return binding.root
    }
}