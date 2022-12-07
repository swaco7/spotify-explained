package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.*
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.BundleItemType

class BundleAdapter(var items: List<BundleGraphItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            BundleItemType.TRACK.value -> {
                val binding = TrackBundleRowNofeaturesBinding.inflate(inflater, parent, false)
                TrackViewHolder(binding)
            }
            BundleItemType.ARTIST.value -> {
                val binding = ArtistBundleRowBinding.inflate(inflater, parent, false)
                ViewHolder(binding)
            }
            else -> {
                val binding = GenreBundleRowBinding.inflate(inflater, parent, false)
                GenreViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].bundleItemType.value
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BundleAdapter.ViewHolder -> {
                holder.bind(items[position])
            }
            is BundleAdapter.TrackViewHolder -> {
                holder.bind(items[position])
            }
            is BundleAdapter.GenreViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    fun updateData(items: List<BundleGraphItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ArtistBundleRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleGraphItem) {
            binding.trackName = item.track?.trackName ?: ""
            binding.artist = item.artist
            binding.genresAdapter = GenresBundleAdapter(item.genreColors)
            binding.executePendingBindings()
        }
    }

    inner class TrackViewHolder(val binding: TrackBundleRowNofeaturesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleGraphItem) {
            binding.trackName = item.track?.trackName ?: ""
            binding.track = item.track
            binding.genresAdapter = GenresBundleAdapter(item.genreColors)
            binding.executePendingBindings()
        }
    }

    inner class GenreViewHolder(val binding: GenreBundleRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleGraphItem) {
            binding.genre = item.genreColors!![0].name
            binding.color = item.genreColors!![0].color
            binding.executePendingBindings()
        }
    }

}