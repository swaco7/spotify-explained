package com.example.spotifyexplained.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.databinding.PageContentItemBinding
import com.example.spotifyexplained.databinding.TextValueItemBinding
import com.example.spotifyexplained.model.home.PageItem
import com.example.spotifyexplained.model.home.PageType
import com.example.spotifyexplained.model.home.StatsSectionItem
import com.example.spotifyexplained.ui.home.HomeFragment
import com.example.spotifyexplained.ui.home.HomeFragmentDirections
import com.example.spotifyexplained.ui.recommend.spotify.RecommendFragmentDirections

class PagesHomeAdapter(var items: List<PageItem>, var homeFragment: HomeFragment) : RecyclerView.Adapter<PagesHomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PageContentItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(items: List<PageItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: PageContentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PageItem) {
            binding.title = item.title
            binding.content = item.content
            binding.detailButton.setOnClickListener {
                when (item.pageType){
                    PageType.RECOMMEND -> NavHostFragment.findNavController(homeFragment).navigate(HomeFragmentDirections.actionNavigationHomeToFragmentCustomRecommend())
                    PageType.USERTOP -> NavHostFragment.findNavController(homeFragment).navigate(HomeFragmentDirections.actionNavigationHomeToNavigationTopSongs())
                    PageType.SPOTIFY -> NavHostFragment.findNavController(homeFragment).navigate(HomeFragmentDirections.actionNavigationHomeToNavigationRecommendedSongs())
                    PageType.PLAYLIST -> NavHostFragment.findNavController(homeFragment).navigate(HomeFragmentDirections.actionNavigationHomeToNavigationPlaylist())
                }
            }
            binding.executePendingBindings()
        }
    }
}