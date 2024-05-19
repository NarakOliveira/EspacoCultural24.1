package com.espaco.cultural.activities.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.database.ArtWorkDB
import com.espaco.cultural.databinding.FragmentCreateArtWorkBinding
import com.espaco.cultural.entities.ArtWork
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import java.io.ByteArrayOutputStream


class CreateArtWorkFragment : Fragment() {
    private lateinit var binding: FragmentCreateArtWorkBinding
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { onImagePick(it) } }
    private var selectedUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateArtWorkBinding.inflate(inflater)
        binding.imageView.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build())
        }

        binding.titleEditText.addTextChangedListener {
            binding.titleInputLayout.error = null
        }

        binding.autorEditText.addTextChangedListener {
            binding.autorInputLayout.error = null
        }

        binding.descriptionEditText.addTextChangedListener {
            binding.descriptionInputLayout.error = null
        }

        binding.button.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val autor = binding.autorEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            if (selectedUri == null) {
                Toast.makeText(requireContext(), "Insira uma imagem da obra", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (title.isEmpty() || title.isBlank()) {
                binding.titleInputLayout.error = "Informe o nome da obra"
                binding.titleEditText.requestFocus()
                return@setOnClickListener
            }

            if (autor.isEmpty() || autor.isBlank()) {
                binding.autorInputLayout.error = "Informe o nome do autor"
                binding.autorEditText.requestFocus()
                return@setOnClickListener
            }

            if (description.isEmpty() || description.isBlank()) {
                binding.descriptionInputLayout.error = "Informe a descrição da obra"
                binding.descriptionEditText.requestFocus()
                return@setOnClickListener
            }

            uploadArtWork(title, autor, description)
        }
        return binding.root
    }

    private fun onImagePick(uri: Uri) {
        selectedUri = uri
        Glide.with(binding.imageView)
            .load(uri)
            .into(binding.imageView)
    }

    private fun uploadArtWork(title: String, autor: String, description: String) {
        if (selectedUri == null) return
        val bitmap = getCapturedImage(selectedUri!!)

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val encoded: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        ArtWorkDB.publishArtWork(ArtWork(title, autor, description, encoded))

        Toast.makeText(requireContext(), "Obra adicionada", Toast.LENGTH_SHORT).show()
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
    }
    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val contentResolver = requireActivity().contentResolver
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }
}