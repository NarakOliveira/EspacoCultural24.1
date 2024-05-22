package com.espaco.cultural.activities.fragments

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.adapters.CommentsAdapter
import com.espaco.cultural.database.CommentsDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.databinding.FragmentFullArtWorkBinding
import com.espaco.cultural.entities.ArtWork

class FullArtWorkFragment : Fragment() {
    private lateinit var binding: FragmentFullArtWorkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = requireArguments()
        val artWork = ArtWork(
            args.getString("id") ?: "None",
            args.getString("title") ?: "None",
            args.getString("autor") ?: "None",
            args.getString("description") ?: "None",
            args.getString("image") ?: "",
        )

        binding = FragmentFullArtWorkBinding.inflate(inflater)

        binding.textView.text = artWork.title
        binding.textView6.text = artWork.autor
        binding.textView5.text = artWork.description

        val imageByteArray: ByteArray = Base64.decode(artWork.image, Base64.DEFAULT)
        Glide.with(this)
            .asBitmap()
            .load(imageByteArray)
            .placeholder(R.drawable.ic_downloading)
            .into(binding.imageView)

        val context = requireContext()
        val adapter = CommentsAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        CommentsDB.getComments(artWork) {comments ->
            val registrations: LinkedHashSet<String> = LinkedHashSet()
            comments.forEach {  registrations.add(it.author) }

            UserDB.findUsers(registrations) { users ->
                adapter.updateData(users, comments)
            }
        }

        return binding.root
    }

}