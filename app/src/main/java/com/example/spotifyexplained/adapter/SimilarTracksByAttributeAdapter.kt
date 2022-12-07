package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.SimilarByAttrRowBinding
import com.example.spotifyexplained.databinding.SimilarByAttrRowTextBinding
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.TrackValue

class SimilarTracksByAttributeAdapter(var items: List<TrackValue>, var trackDetailClickHandler: TrackDetailClickHandler) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val binding = SimilarByAttrRowBinding.inflate(inflater, parent, false)
                ViewHolder(binding)
            }
            else -> {
                val binding = SimilarByAttrRowTextBinding.inflate(inflater, parent, false)
                ViewHolderNoImage(binding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SimilarTracksByAttributeAdapter.ViewHolder -> {
                holder.bind(items[position])
            }
            is SimilarTracksByAttributeAdapter.ViewHolderNoImage -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].imageUrl != null) 0 else 1
    }

    fun updateData(items: List<TrackValue>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: SimilarByAttrRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackValue) {
            binding.name = item.name
            binding.value = String.format("%d", item.value.toInt())
            binding.artist = item.artistName
            binding.imageUrl = item.imageUrl
            binding.trackId = item.trackId
            binding.clickHandler = trackDetailClickHandler
            binding.executePendingBindings()
        }
    }

    inner class ViewHolderNoImage(val binding: SimilarByAttrRowTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackValue) {
            binding.name = item.name
            binding.value = String.format("%d", item.value.toInt())
            binding.executePendingBindings()
        }
    }
}