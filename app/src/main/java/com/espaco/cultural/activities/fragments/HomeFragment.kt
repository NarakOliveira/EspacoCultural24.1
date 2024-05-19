package com.espaco.cultural.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.adapters.ArtWorkAdapter
import com.espaco.cultural.database.ArtWorkDB
import com.espaco.cultural.database.preferences.UserPreferences
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

        val context = requireContext()
        val userPreferences = UserPreferences(context)

        binding = FragmentHomeBinding.inflate(layoutInflater)
        val navigationView = requireActivity().findViewById<NavigationView>(R.id.navigationView)

        if (userPreferences.isAdmin) {
            binding.floatingActionButton2.visibility = View.VISIBLE
            binding.floatingActionButton2.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragmentContainer, CreateArtWorkFragment())
                    .commit()
            }
        } else binding.floatingActionButton2.visibility = View.GONE

        binding.floatingActionButton.setOnClickListener {
            navigationView.setCheckedItem(R.id.nav_visits)
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragmentContainer, VisitsFragment())
                .commit()
        }

        val adapter = ArtWorkAdapter()
        adapter.setOnArtWorkClicked {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragmentContainer, FullArtWorkFragment(it))
                .commit()
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        ArtWorkDB.getArtWorks {
            adapter.updateData(it)
            binding.progress.visibility = View.GONE
        }

        return binding.root;
    }
}