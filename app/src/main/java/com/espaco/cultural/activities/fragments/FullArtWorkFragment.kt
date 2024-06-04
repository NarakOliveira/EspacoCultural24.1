package com.espaco.cultural.activities.fragments

import android.content.res.Resources
import android.graphics.Point
import android.os.Bundle
import android.util.Base64
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.activities.MainActivity
import com.espaco.cultural.adapters.CommentsAdapter
import com.espaco.cultural.database.ArtWorkDB
import com.espaco.cultural.database.CommentsDB
import com.espaco.cultural.database.NotificationDB
import com.espaco.cultural.database.UserDB
import com.espaco.cultural.database.preferences.UserPreferences
import com.espaco.cultural.databinding.FragmentFullArtWorkBinding
import com.espaco.cultural.entities.ArtWork
import com.espaco.cultural.entities.Comment
import com.espaco.cultural.entities.Notification
import com.espaco.cultural.entities.User
import java.io.InputStream
import java.util.Date


class FullArtWorkFragment : Fragment() {
    private lateinit var binding: FragmentFullArtWorkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = requireArguments()
        val artWorkId = args.getString("id") ?: "None"
        //val artWork = ArtWork(
        //    args.getString("id") ?: "None",
        //    args.getString("title") ?: "None",
        //    args.getString("autor") ?: "None",
        //    args.getString("description") ?: "None",
        //    args.getString("image") ?: "",
        //)

        binding = FragmentFullArtWorkBinding.inflate(inflater)

        ArtWorkDB.findArtWork(artWorkId) {
            val artWork = it ?: ArtWork("None", "None", "None", "None", "")
            binding.textView.text = artWork.title
            binding.textView6.text = artWork.autor
            binding.textView5.text = artWork.description

            val imageByteArray: ByteArray = Base64.decode(artWork.image, Base64.DEFAULT)
            val width = Resources.getSystem().displayMetrics.widthPixels
            Glide.with(this)
                .asBitmap()
                .load(imageByteArray)
                .placeholder(R.drawable.ic_downloading)
                .override(width)
                .into(binding.imageView)

            val context = requireContext()
            val userPreferences = UserPreferences(context)

            val adapter = CommentsAdapter(userPreferences.registration)
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)

            (activity as MainActivity).binding.imageEdit.setOnClickListener {
                if (!userPreferences.isAdmin) return@setOnClickListener
                val ldf = CreateArtWorkFragment()
                args.clear()
                args.putString("id", artWork.id)
                ldf.setArguments(args)

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.fragmentContainer, ldf)
                    .commit()
            }

            adapter.setOnLikeClicked {
                val newComment = it
                val userIndex = newComment.usersLiked.indexOf(userPreferences.registration)
                if (userIndex != -1) {
                    CommentsDB.likeComment(artWork, it, userPreferences.registration, false)
                    newComment.usersLiked.removeAt(userIndex)
                } else {
                    if (!it.onceLiked.contains(userPreferences.registration)) {
                        NotificationDB.pushNotification(
                            it.author, Notification(
                                "${it.usersLiked.size + 1} Likes",
                                "${userPreferences.name} deu like no seu comentario na obra ${artWork.title}",
                                NotificationDB.TYPE_INTERACTION,
                                Date().time
                            )
                        )
                        newComment.onceLiked.add(userPreferences.registration)
                    }
                    CommentsDB.likeComment(artWork, it, userPreferences.registration, true)
                    newComment.usersLiked.add(userPreferences.registration)
                }
                adapter.updateComment(it, newComment)
            }

            CommentsDB.getComments(artWork) { comments ->
                val registrations: LinkedHashSet<String> = LinkedHashSet()
                comments.forEach { registrations.add(it.author) }

                UserDB.findUsers(registrations) { users ->
                    comments.sortWith(compareBy { it.usersLiked.size })
                    adapter.updateData(users, comments)
                }
            }

            binding.floatingActionButton3.setOnClickListener {
                val content = binding.commentEditText.text.toString()
                if (content.isEmpty() || content.isBlank()) {
                    binding.commentInputLayout.requestFocus()
                    return@setOnClickListener
                }
                binding.commentEditText.setText("")

                val user = User(
                    userPreferences.registration,
                    userPreferences.name,
                    userPreferences.picture,
                    userPreferences.isAdmin
                )
                val comment = CommentsDB.publishComment(
                    artWork,
                    Comment("", user.registration, content, ArrayList(), ArrayList())
                )
                adapter.addComment(user, comment)
            }
        }

        return binding.root
    }

}