package com.example.spotifyexplained.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.databinding.GenreLabelBinding
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.Track

class GenresAdapter(var items: List<String>, var trackDetailClickHandler: TrackDetailClickHandler, var track: Track) : RecyclerView.Adapter<GenresAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = GenreLabelBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(items: List<String>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: GenreLabelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.genre = item
            binding.clickHandler = trackDetailClickHandler
            binding.track = track
            Log.e("genres", items.toString())
            binding.executePendingBindings()
        }
    }
}