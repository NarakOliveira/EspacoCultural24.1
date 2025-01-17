package com.espaco.cultural.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.entities.Comment
import com.espaco.cultural.entities.User

class VisitorsAdapter : RecyclerView.Adapter<VisitorsAdapter.VisitorsHolder>() {
    private var visitors : ArrayList<User> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorsHolder {
        return VisitorsHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_visitor, parent, false))
    }

    override fun onBindViewHolder(holder: VisitorsHolder, position: Int) {
        val user = visitors[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return visitors.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(visitors: ArrayList<User>) {
        this.visitors = visitors
        notifyDataSetChanged()
    }

    class VisitorsHolder(itemView: View) : ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.nameText)
        private val textRegistration: TextView = itemView.findViewById(R.id.registrationText)
        private val imageUser: ImageView = itemView.findViewById(R.id.userImage)
        fun bind(user: User) {
            textName.text = user.name
            textRegistration.text = user.registration

            Glide.with(imageUser)
                .load(user.picture)
                .circleCrop()
                .placeholder(R.drawable.no_user_picture)
                .error(R.drawable.no_user_picture)
                .into(imageUser)
        }
    }
}