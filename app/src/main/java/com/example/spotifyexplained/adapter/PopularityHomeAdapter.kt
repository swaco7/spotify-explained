package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.PopularityTrackRowHomeBinding
import com.example.spotifyexplained.model.home.StatsSectionItem

class PopularityHomeAdapter(var items: List<StatsSectionItem>?) : RecyclerView.Adapter<PopularityHomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PopularityTrackRowHomeBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items?.get(position))
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    fun updateData(items: List<StatsSectionItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: PopularityTrackRowHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatsSectionItem?) {
            binding.track = item?.name
            binding.value = String.format("%d", item?.value?.toInt())
            binding.executePendingBindings()
        }
    }
}