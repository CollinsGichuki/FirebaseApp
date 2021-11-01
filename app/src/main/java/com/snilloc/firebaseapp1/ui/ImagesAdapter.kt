package com.snilloc.firebaseapp1.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snilloc.firebaseapp1.databinding.ActivityAddImageBinding

class ImagesAdapter(private val urls: List<String>) :
    RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {
    class ImagesViewHolder(private val binding: ActivityAddImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun bind(url : String) {
                Glide.with(binding.root).load(url).into(binding.image1)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val binding = ActivityAddImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ImagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val image = urls[position]

        holder.bind(image)
    }

    override fun getItemCount() = urls.size
}