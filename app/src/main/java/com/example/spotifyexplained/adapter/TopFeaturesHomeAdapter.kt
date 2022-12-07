package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.TextValueItemBinding
import com.example.spotifyexplained.model.AudioFeature

class TopFeaturesHomeAdapter(var items: List<AudioFeature>) : RecyclerView.Adapter<TopFeaturesHomeAdapter.ViewHolder>() {

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

    fun updateData(items: List<AudioFeature>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TextValueItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioFeature) {
            binding.name = item.name
            binding.value = String.format("%.2f", item.value)
            binding.executePendingBindings()
        }
    }
}