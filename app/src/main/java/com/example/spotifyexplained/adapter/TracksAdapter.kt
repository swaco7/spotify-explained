package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.TracksRowBinding
import com.example.spotifyexplained.model.Track
import com.example.spotifyexplained.general.TrackDetailClickHandler

class TracksAdapter(var items: List<Track>?, var trackDetailClickHandler: TrackDetailClickHandler) : RecyclerView.Adapter<TracksAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TracksRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items?.get(position))
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    fun updateData(items: List<Track>?) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TracksRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Track?) {
            binding.track = item
            binding.clickHandler = trackDetailClickHandler
            binding.executePendingBindings()
        }
    }
}