package com.espaco.cultural.activities.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.espaco.cultural.R
import com.espaco.cultural.adapters.NotificationAdapter
import com.espaco.cultural.database.NotificationDB
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Notificações" }

        val context = requireContext()
        val userPreferences = UserPreferences(context)

        val adapter = NotificationAdapter()
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        NotificationDB.getNotifications(userPreferences.registration) {
            if (it.isEmpty()) binding.layout.visibility = View.VISIBLE
            else binding.layout.visibility = View.GONE
            adapter.updateData(it)
        }

        return binding.root
    }
}