package com.espaco.cultural.activities.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.activities.MainActivity
import com.espaco.cultural.adapters.ArtWorkAdapter
import com.espaco.cultural.database.ArtWorkDB
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentHomeBinding
import com.espaco.cultural.entities.ArtWork
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private lateinit var artWorkList: ArrayList<ArtWork>
    private lateinit var artWorkAdapter: ArtWorkAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        val context = requireContext()
        val userPreferences = UserPreferences(context)

        val activity = requireActivity() as AppCompatActivity
        val actionBar = activity.supportActionBar
        actionBar?.apply {
            title = "Espaço Cultural"
        }
        val searchView = (activity as MainActivity).binding.searchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) filterList(newText.lowercase())
                return false
            }
        })

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

        artWorkAdapter = ArtWorkAdapter()
        artWorkAdapter.setOnArtWorkClicked {
            val ldf = FullArtWorkFragment()
            val args = Bundle()
            args.putString("id", it.id)
            args.putString("title", it.title)
            args.putString("autor", it.autor)
            args.putString("description", it.description)
            args.putString("image", it.image)
            ldf.setArguments(args)

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragmentContainer, ldf)
                .commit()
        }

        binding.recyclerView.adapter = artWorkAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        ArtWorkDB.getArtWorks {
            artWorkList = it
            artWorkAdapter.updateData(it)
            binding.progress.visibility = View.GONE
        }

        return binding.root;
    }
    private fun filterList(text: String) {
        if (text.isEmpty()) {
            artWorkAdapter.updateData(artWorkList)
            return
        }

        val filteredList = ArrayList<ArtWork>()
        for (artWork in artWorkList) {
            if (artWork.title.lowercase().contains(text)) filteredList.add(artWork)
            else if (artWork.description.lowercase().contains(text)) filteredList.add(artWork)
            else if (artWork.autor.lowercase().contains(text)) filteredList.add(artWork)
        }

        artWorkAdapter.updateData(filteredList)
    }
}