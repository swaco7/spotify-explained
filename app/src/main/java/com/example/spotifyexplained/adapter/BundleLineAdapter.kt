package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.BundleFeatureItemBinding
import com.example.spotifyexplained.databinding.BundleGenreItemBinding
import com.example.spotifyexplained.databinding.BundleRelatedItemBinding
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.BundleLineInfo
import com.example.spotifyexplained.model.enums.LinkType

class BundleLineAdapter(var items: List<BundleLineInfo>, var trackDetailClickHandler: TrackDetailClickHandler? = null) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val binding = BundleRelatedItemBinding.inflate(inflater, parent, false)
                ViewHolderRelated(binding)
            }
            1 -> {
                val binding = BundleFeatureItemBinding.inflate(inflater, parent, false)
                ViewHolderFeature(binding)
            }
            else -> {
                val binding = BundleGenreItemBinding.inflate(inflater, parent, false)
                ViewHolderGenre(binding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BundleLineAdapter.ViewHolderRelated -> {
                holder.bind(items[position])
            }
            is BundleLineAdapter.ViewHolderFeature -> {
                holder.bind(items[position])
            }
            is BundleLineAdapter.ViewHolderGenre -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].linkType) {
            LinkType.RELATED -> 0
            LinkType.FEATURE -> 1
            else -> 2
        }
    }

    fun updateData(items: List<BundleLineInfo>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolderRelated(val binding: BundleRelatedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleLineInfo) {
            binding.bundleLineInfo = item
            binding.executePendingBindings()
        }
    }

    inner class ViewHolderFeature(val binding: BundleFeatureItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleLineInfo) {
            binding.bundleLineInfo = item
            binding.value = String.format("%.2f", item.features?.second)
            binding.executePendingBindings()
        }
    }

    inner class ViewHolderGenre(val binding: BundleGenreItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BundleLineInfo) {
            binding.bundleLineInfo = item
            binding.executePendingBindings()
        }
    }
}