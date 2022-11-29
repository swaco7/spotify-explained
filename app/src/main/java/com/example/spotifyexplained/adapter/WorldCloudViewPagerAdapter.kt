package com.example.spotifyexplained.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.spotifyexplained.databinding.PageContentItemBinding
import com.example.spotifyexplained.databinding.WordcloudItemBinding
import com.example.spotifyexplained.general.App.Companion.context
import com.example.spotifyexplained.model.home.PageItem
import com.example.spotifyexplained.model.home.WordItemBundle
import com.example.spotifyexplained.services.WordCloud
import java.util.*

class WorldCloudViewPagerAdapter(var items: List<WordItemBundle>) : RecyclerView.Adapter<WorldCloudViewPagerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorldCloudViewPagerAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WordcloudItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorldCloudViewPagerAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: WordcloudItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WordItemBundle) {
            binding.title.text = item.title
            val chartWebView = binding.wordCloud
            val displayMetrics = context.resources.displayMetrics
            chartWebView.settings.javaScriptEnabled = true
            val rawHtml = WordCloud.getHeader() +
                    "var data = ${item.content}; \n" +
                    WordCloud.getBody((displayMetrics.widthPixels / displayMetrics.density).toInt() - 30) +
                    WordCloud.getFooter()
            val encodedHtml = Base64.getEncoder().encodeToString(rawHtml.toByteArray())
            chartWebView.loadData(encodedHtml, "text/html", "base64")
            binding.executePendingBindings()
        }
    }
}