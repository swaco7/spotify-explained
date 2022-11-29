package com.example.spotifyexplained.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.TextValueItemBinding
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.Track
import com.example.spotifyexplained.model.home.StatsSectionItem
import com.example.spotifyexplained.ui.recommend.custom.base.CustomRecommendBaseFragmentDirections

class TopGenresHomeAdapter(var items: List<StatsSectionItem>, var clickHandler: TrackDetailClickHandler) : RecyclerView.Adapter<TopGenresHomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TextValueItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(items: List<StatsSectionItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TextValueItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatsSectionItem) {
            binding.name = item.name
            binding.value = String.format("%d", item.value.toInt())
            binding.artist = item.artistName
            binding.imageUrl = item.imageUrl
            binding.trackId = item.trackId
            if (item.trackId != null) {
                binding.clickHandler = clickHandler
            }
            Log.e("genres", items.toString())
            binding.executePendingBindings()
        }
    }
}