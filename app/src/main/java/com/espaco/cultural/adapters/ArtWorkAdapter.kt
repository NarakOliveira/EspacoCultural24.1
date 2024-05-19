package com.espaco.cultural.adapters

import android.annotation.SuppressLint
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.espaco.cultural.R
import com.espaco.cultural.entities.ArtWork
import com.espaco.cultural.entities.Horario

class ArtWorkAdapter: RecyclerView.Adapter<ArtWorkAdapter.ArtWorkHolder>() {
    private var artWorks: ArrayList<ArtWork> = ArrayList()
    private lateinit var onArtWorkClicked: (artWork: ArtWork) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtWorkHolder {
        return ArtWorkHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_artwork, parent, false))
    }

    override fun onBindViewHolder(holder: ArtWorkHolder, position: Int) {
        val artWork = artWorks[position]
        holder.bind(artWork)

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && this::onArtWorkClicked.isInitialized) {
                this.onArtWorkClicked(artWork)
            }
        }
    }

    override fun getItemCount(): Int {
        return artWorks.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(artWorks: ArrayList<ArtWork>) {
        this.artWorks = artWorks
        notifyDataSetChanged()
    }

    fun setOnArtWorkClicked(onArtWorkClicked: (artWork: ArtWork) -> Unit) {
        this.onArtWorkClicked = onArtWorkClicked
    }

    class ArtWorkHolder(itemView: View) : ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textView)
        private val textAutor: TextView = itemView.findViewById(R.id.textView6)
        private val textDescription : TextView = itemView.findViewById(R.id.textView5)
        private val image : ImageView = itemView.findViewById(R.id.imageView)
        fun bind(artWork: ArtWork) {
            textTitle.text = artWork.title
            textAutor.text = artWork.autor
            textDescription.text = artWork.description

            val imageByteArray: ByteArray = Base64.decode(artWork.image, Base64.DEFAULT)
            Glide.with(image)
                .asBitmap()
                .load(imageByteArray)
                .into(image)
        }
    }
}