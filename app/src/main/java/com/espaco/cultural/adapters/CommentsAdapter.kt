package com.espaco.cultural.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.entities.Comment
import com.espaco.cultural.entities.User

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentsHolder>() {
    private var comments: ArrayList<Comment> = ArrayList()
    private var users: HashMap<String, User> = HashMap()
    private lateinit var onLikeClicked: (comment: Comment) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsHolder {
        return CommentsHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentsHolder, position: Int) {
        val comment = comments[position]
        val author = users[comment.author]
        holder.bind(author, comment)

        holder.buttonLike.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && this::onLikeClicked.isInitialized) {
                this.onLikeClicked(comment)
            }
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(users: HashMap<String, User>, comments: ArrayList<Comment>) {
        this.comments = comments;
        this.users = users;
        notifyDataSetChanged()
    }

    fun addComment(user: User, comment: Comment) {
        comments.add(comment)
        users[comment.author] = user
        notifyItemInserted(comments.size - 1)
    }

    fun setOnLikeClicked(onLikeClicked: (comment: Comment) -> Unit) {
        this.onLikeClicked = onLikeClicked
    }

    class CommentsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textContent: TextView = itemView.findViewById(R.id.textContent)
        private val textLike: TextView = itemView.findViewById(R.id.textLike)
        private val imageUser: ImageView = itemView.findViewById(R.id.imageUser)
        val buttonLike: ImageView = itemView.findViewById(R.id.buttonLike)

        fun bind(user: User?, comment: Comment) {
            textName.text = user?.name ?: "None"
            textContent.text = comment.content
            textLike.text = comment.usersLiked.size.toString()

            Glide.with(imageUser)
                .load(user?.picture ?: "None")
                .placeholder(R.drawable.no_user_picture)
                .error(R.drawable.no_user_picture)
                .into(imageUser)
        }
    }
}