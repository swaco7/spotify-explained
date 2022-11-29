package com.example.spotifyexplained.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.FeatureInfoRowBinding
import com.example.spotifyexplained.databinding.NameValueItemBinding
import com.example.spotifyexplained.databinding.TextValueItemBinding
import com.example.spotifyexplained.model.AudioFeature
import com.example.spotifyexplained.model.AudioFeatureType
import com.example.spotifyexplained.model.AudioFeatures

class FeaturesLineInfoAdapter(var items: List<Pair<String, Double>>) : RecyclerView.Adapter<FeaturesLineInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FeatureInfoRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    fun updateData(items: List<Pair<String, Double>>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: FeatureInfoRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<String, Double>) {
            binding.name = item.first
            binding.value = String.format("%.2f", item.second)
            binding.executePendingBindings()
        }
    }
}