package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.*
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.BundleItemType

class TrackFeatureBundleAdapter(var items: List<BundleTrackFeatureItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            BundleItemType.TRACK.value -> {
                val binding = TrackBundleRowBinding.inflate(inflater, parent, false)
                ViewHolder(binding)
            }
            else -> {
                val binding = NameItemBinding.inflate(inflater, parent, false)
                FeatureViewHolder(binding)
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
            is TrackFeatureBundleAdapter.ViewHolder -> {
                holder.bind(items[position])
            }
            is TrackFeatureBundleAdapter.FeatureViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    fun updateData(items: List<BundleTrackFeatureItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TrackBundleRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleTrackFeatureItem) {
            binding.track = item.track
            binding.featuresAdapter = FeaturesAdapter(item.audioFeatures.sortedByDescending { it.second })
            binding.executePendingBindings()
        }
    }

    inner class FeatureViewHolder(val binding: NameItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleTrackFeatureItem) {
            binding.name = item.audioFeatureType!!.name
            binding.executePendingBindings()
        }
    }

}