package com.espaco.cultural.activities.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.espaco.cultural.R
import com.espaco.cultural.databinding.FragmentNfcBinding

class NfcFragment : Fragment() {
    private lateinit var binding: FragmentNfcBinding
    private lateinit var intent: Intent

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNfcBinding.inflate(inflater)

        val activity = requireActivity()
        //val context = requireContext()
        intent = activity.intent

        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.apply { title = "Detectar obra" }

        //if (intent.action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
        //    val rawMessages = getNfcMessage()
        //    if (rawMessages != null) {
        //        val value = String((rawMessages[0] as NdefMessage).records[0].payload)
        //        Toast.makeText(context, value, Toast.LENGTH_SHORT).show()
        //    }
        //} else {
        //    val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

        //    if (nfcAdapter == null) {
        //        binding.imageIcon.setImageResource(R.drawable.ic_sensors_off)
        //        binding.textMessage.text = "Não suporta nfc"
        //    }
        //}


        return binding.root
    }

    //private fun getNfcMessage(): Array<out Parcelable>? {
    //    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    //        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, Parcelable::class.java)
    //    } else {
    //        return intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
    //    }
    //}
}