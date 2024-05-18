package com.espaco.cultural.activities.fragments

import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.espaco.cultural.R
import com.espaco.cultural.databinding.FragmentHomeBinding
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Espaço Cultural" }

        binding = FragmentHomeBinding.inflate(layoutInflater)
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.navigationView)


        binding.floatingActionButton.setOnClickListener {
            navigationView.setCheckedItem(R.id.nav_visits)
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragmentContainer, VisitsFragment())
                .commit()
        }

        return binding.root;
    }

    override fun onResume() {
        super.onResume()
    }
}