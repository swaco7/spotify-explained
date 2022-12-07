package com.example.spotifyexplained.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.FeatureSettingsRowBinding
import com.example.spotifyexplained.model.AudioFeature
import com.example.spotifyexplained.ui.recommend.custom.settings.CustomRecommendSettingsViewModel

class FeatureSettingsAdapter(var items: MutableList<AudioFeature>, val recommendSettingsViewModel: CustomRecommendSettingsViewModel) : RecyclerView.Adapter<FeatureSettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FeatureSettingsRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    fun updateData(items: MutableList<AudioFeature>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: FeatureSettingsRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioFeature) {
            binding.featureName = item.name
            binding.featureIndex = adapterPosition
            binding.viewModel = recommendSettingsViewModel
            binding.executePendingBindings()
        }
    }
}