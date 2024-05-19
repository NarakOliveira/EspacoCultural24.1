package com.espaco.cultural.activities.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.espaco.cultural.database.preferences.SettingsPreferences
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentSettingsBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var ctx: Context

    private lateinit var userPreferences: UserPreferences
    private lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireContext()
        userPreferences = UserPreferences(ctx)
        settingsPreferences = SettingsPreferences(ctx)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Configurações" }

        binding = FragmentSettingsBinding.inflate(inflater)

        binding.needPasswordSwitch.isChecked = settingsPreferences.needPassword
        binding.newExposureSwitch.isChecked = settingsPreferences.newExposition
        binding.interactionsSwitch.isChecked = settingsPreferences.interactions

        binding.needPasswordSwitch.setOnCheckedChangeListener { _, status ->  settingsPreferences.needPassword = status}
        binding.newExposureSwitch.setOnCheckedChangeListener { _, status ->
            settingsPreferences.newExposition = status
            if (status) Firebase.messaging.subscribeToTopic("exposition")
            else Firebase.messaging.unsubscribeFromTopic("exposition")
        }
        binding.interactionsSwitch.setOnCheckedChangeListener { _, status ->  settingsPreferences.interactions = status}

        return binding.root
    }
}