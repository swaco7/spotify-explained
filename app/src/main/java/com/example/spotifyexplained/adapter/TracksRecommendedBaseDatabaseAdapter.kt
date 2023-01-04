package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.database.entity.BaseTrackEntity
import com.example.spotifyexplained.databinding.TracksSimplifiedRowBinding
import com.example.spotifyexplained.model.Track
import com.example.spotifyexplained.general.TrackDetailClickHandler

class TracksRecommendedBaseDatabaseAdapter<T : BaseTrackEntity>(var trackDetailClickHandler: TrackDetailClickHandler) : ListAdapter<T, TracksRecommendedBaseDatabaseAdapter<T>.ViewHolder>(TracksComparator<T>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TracksSimplifiedRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position).track)
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

    class TracksComparator<T : BaseTrackEntity> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.track == newItem.track
        }
    }
}