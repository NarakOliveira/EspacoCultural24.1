package com.espaco.cultural.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.espaco.cultural.databinding.FragmentCommentsBinding

class CommentsFragment : Fragment() {

    private lateinit var binding: FragmentCommentsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Comentarios" }

        binding = FragmentCommentsBinding.inflate(layoutInflater)

        return binding.root;
    }


    override fun onResume() {
        super.onResume()
    }
}
