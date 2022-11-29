package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.ArtistLabelBinding
import com.example.spotifyexplained.model.Artist

class RelatedArtistsAdapter(var items: List<Artist>) : RecyclerView.Adapter<RelatedArtistsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArtistLabelBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(items: List<Artist>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ArtistLabelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Artist) {
            binding.artist = item
            binding.executePendingBindings()
        }
    }
}