package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.SimilarTracksRowBinding
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.SimilarTrack

class SimilarTracksAdapter(var items: List<SimilarTrack>, var trackDetailClickHandler: TrackDetailClickHandler) : RecyclerView.Adapter<SimilarTracksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SimilarTracksRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(items: List<SimilarTrack>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: SimilarTracksRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimilarTrack) {
            binding.track = item.track?.track
            binding.trackName = item.name
            binding.color = item.color
            binding.similarity = item.similarity
            binding.clickHandler = trackDetailClickHandler
            binding.executePendingBindings()
        }
    }
}