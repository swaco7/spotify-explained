package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.TracksSimplifiedRowBinding
import com.example.spotifyexplained.model.Track
import com.example.spotifyexplained.general.TrackDetailClickHandler

class TracksRecommendedAdapter(var items: List<Track>, var trackDetailClickHandler: TrackDetailClickHandler) : RecyclerView.Adapter<TracksRecommendedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TracksSimplifiedRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(items: List<Track>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TracksSimplifiedRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Track) {
            binding.track = item
            binding.clickHandler = trackDetailClickHandler
            if (item.trackGenres != null) {
                binding.genresAdapter = GenresAdapter((item.trackGenres)!!.asList(), trackDetailClickHandler, item)
            }
            binding.executePendingBindings()
        }
    }
}