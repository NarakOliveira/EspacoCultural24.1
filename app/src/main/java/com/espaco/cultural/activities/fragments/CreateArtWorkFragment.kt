package com.espaco.cultural.activities.fragments

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.appcompat.app.AlertDialog
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
    private lateinit var context: Context

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { onImagePick(it) } }
    private var image64: String? = null
    private var artWorkId = ""
    private var isEditing = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateArtWorkBinding.inflate(inflater)
        context = requireContext()

        val activity = requireActivity() as MainActivity
        isEditing = arguments != null && requireArguments().containsKey("id")

        if (isEditing) {
            binding.progress.visibility = View.VISIBLE
            artWorkId = arguments?.getString("id") ?: ""
            updateDataFromDB(artWorkId)

            binding.button.text = "Alterar obra"
            activity.binding.title.text = "Editar obra"
            activity.binding.imageDelete.visibility = View.VISIBLE

            activity.binding.imageDelete.setOnClickListener {
                confirmDialog("Deletar obra", "Tem certeza que deseja deletar essa obra?") {
                    ArtWorkDB.deleteArtWork(artWorkId)
                    Toast.makeText(context, "Obra deletada", Toast.LENGTH_SHORT).show()

                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                }
            }
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
                Toast.makeText(context, "Insira uma imagem da obra", Toast.LENGTH_SHORT).show()
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

            val dialogTitle = if (isEditing) "Editar obra" else "Adicionar obra"
            val dialogMessage = if (isEditing) "Tem certeza que deseja editar essa obra?"
            else "Tem certeza que deseja adicionar essa obra?"

            confirmDialog(dialogTitle, dialogMessage) { uploadArtWork(title, autor, description) }
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
        ArtWorkDB.publishArtWork(ArtWork(artWorkId, title, autor, description, image64!!))

        if (isEditing) {
            Toast.makeText(context, "Obra editada", Toast.LENGTH_SHORT).show()
        } else {
            UserDB.getAllUserRegistration {
                val notification = Notification(
                    "Nova obra adicionada",
                    "Obra adicionada: $title Autor: $autor",
                    NotificationDB.TYPE_EXPOSITION,
                    Date().time
                )
                NotificationDB.broadcastNotification(it, notification)
            }
            Toast.makeText(context, "Obra adicionada", Toast.LENGTH_SHORT).show()
        }

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

            binding.progress.visibility = View.GONE
        }
    }

    private fun confirmDialog(title: String, description: String, callback: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton("Confirmar") {_, _ -> callback() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}