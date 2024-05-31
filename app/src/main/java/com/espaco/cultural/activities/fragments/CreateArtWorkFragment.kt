package com.espaco.cultural.activities.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.espaco.cultural.activities.MainActivity
import com.espaco.cultural.database.ArtWorkDB
import com.espaco.cultural.database.NotificationDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.databinding.FragmentCreateArtWorkBinding
import com.espaco.cultural.entities.ArtWork
import com.espaco.cultural.entities.Notification
import java.io.ByteArrayOutputStream
import java.util.Date


class CreateArtWorkFragment : Fragment() {
    private lateinit var binding: FragmentCreateArtWorkBinding
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { onImagePick(it) } }
    private var image64: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateArtWorkBinding.inflate(inflater)

        val activity = requireActivity() as MainActivity
        val isEditing = arguments != null && requireArguments().containsKey("id")

        if (isEditing) {
            val artWorkId = arguments?.getString("id")
            updateDataFromDB(artWorkId!!)

            binding.button.text = "Alterar obra"
            activity.binding.title.text = "Editar obra"
            activity.binding.imageDelete.visibility = View.VISIBLE
        }

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

            if (image64 == null) {
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
        updateImage64(uri)
        Glide.with(binding.imageView)
            .load(uri)
            .into(binding.imageView)
    }

    private fun uploadArtWork(title: String, autor: String, description: String) {
        if (image64 == null) return
        ArtWorkDB.publishArtWork(ArtWork("", title, autor, description, image64!!))

        UserDB.getAllUserRegistration {
            val notification = Notification(
                "Nova obra adicionada",
                "Obra adicionada: $title Autor: $autor",
                NotificationDB.TYPE_EXPOSITION,
                Date().time
            )
            NotificationDB.broadcastNotification(it, notification)
        }
        Toast.makeText(requireContext(), "Obra adicionada", Toast.LENGTH_SHORT).show()
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()
    }

    private fun updateImage64(uri: Uri) {
        val bitmap = getCapturedImage(uri)

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        image64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
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

    @SuppressLint("SetTextI18n")
    private fun updateDataFromDB(id: String) {
        ArtWorkDB.findArtWork(id) {
            if (it == null) return@findArtWork

            image64 = it.image
            binding.titleEditText.setText(it.title)
            binding.descriptionEditText.setText(it.description)
            binding.autorEditText.setText(it.autor)

            val imageByteArray: ByteArray = Base64.decode(it.image, Base64.DEFAULT)
            Glide.with(binding.imageView)
                .asBitmap()
                .load(imageByteArray)
                .into(binding.imageView)
        }
    }
}