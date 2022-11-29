package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.GenreColorRowBinding
import com.example.spotifyexplained.databinding.SimilarTracksRowBinding
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.GenreColor
import com.example.spotifyexplained.model.SimilarTrack

class GenreColorAdapter(var items: List<GenreColor>?) : RecyclerView.Adapter<GenreColorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GenreColorRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items?.get(position))
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    fun updateData(items: List<GenreColor>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: GenreColorRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GenreColor?) {
            binding.genreName = item?.name
            binding.color = item?.color
            binding.executePendingBindings()
        }
    }
}