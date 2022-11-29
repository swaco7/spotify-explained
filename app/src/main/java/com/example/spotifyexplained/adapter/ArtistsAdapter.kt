package com.example.spotifyexplained.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.ArtistRowBinding
import com.example.spotifyexplained.model.Artist

class ArtistsAdapter(var items: List<Artist>) : RecyclerView.Adapter<ArtistsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArtistRowBinding.inflate(inflater, parent, false)
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

    inner class ViewHolder(val binding: ArtistRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Artist) {
            Log.e("imgUrl", item.images!![0].url)
            binding.artist = item
            binding.executePendingBindings()
        }
    }
}