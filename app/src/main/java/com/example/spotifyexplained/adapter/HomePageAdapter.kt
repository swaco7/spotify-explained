package com.example.spotifyexplained.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.spotifyexplained.R
import com.example.spotifyexplained.databinding.*
import com.example.spotifyexplained.general.App.Companion.context
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.AudioFeature
import com.example.spotifyexplained.model.home.*
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import com.example.spotifyexplained.ui.home.HomeFragment
import com.synnapps.carouselview.ViewListener


class HomePageAdapter(var items: List<HomeSection>, var homeFragment: HomeFragment, val clickHandler: TrackDetailClickHandler) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val contentsViewListener = ViewListener { position ->
            when (position) {
                0 -> inflater.inflate(R.layout.page_custom_recomm, null)
                1 -> inflater.inflate(R.layout.page_user_music, null)
                2 -> inflater.inflate(R.layout.page_spotify_recomm, null)
                else -> inflater.inflate(R.layout.page_playlist, null)
            }
        }

        val wordCloudsViewListener = ViewListener { position ->
            when (position) {
                0 -> inflater.inflate(R.layout.page_custom_recomm, null)
                1 -> inflater.inflate(R.layout.page_user_music, null)
                2 -> inflater.inflate(R.layout.page_spotify_recomm, null)
                else -> inflater.inflate(R.layout.page_playlist, null)
            }
        }

        return when (viewType) {
            HomeSectionType.STATS.value -> {
                val binding = SectionHomeStatisticsBinding.inflate(inflater, parent, false)
                UserStatisticsViewHolder(binding)
            }
            HomeSectionType.APPCONTENTS.value -> {
                val binding = SectionHomeAppcontentsBinding.inflate(inflater, parent, false)
                AppContentsViewHolder(binding, contentsViewListener)
            }
            HomeSectionType.WORDCLOUD.value -> {
                val binding = SectionHomeWordcloudBinding.inflate(inflater, parent, false)
                WordCloudsViewHolder(binding)
            }
            else -> {
                val binding = SectionHomeTracksBinding.inflate(inflater, parent, false)
                LeastPopularViewHolder(binding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserStatisticsViewHolder -> {
                holder.bind(items[position] as HomeStatsSection)
            }
            is AppContentsViewHolder -> {
                holder.bind(items[position] as AppContentsSection)
            }
            is WordCloudsViewHolder -> {
                holder.bind(items[position] as WordCloudsSection)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type.value
    }

    fun updateData(items: List<HomeSection>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: SectionHomeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AudioFeature) {
            //binding.genre = item.name
            //binding.value = String.format("%.2f", item.value)
            Log.e("genres", items.toString())
            binding.executePendingBindings()
        }
    }
    inner class LeastPopularViewHolder(val binding: SectionHomeTracksBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatsSection) {
            binding.tracksExpanded = false
            binding.title = context.getString(R.string.least_popular)
            if (item.items.isNotEmpty()) {
                binding.adapter = PopularityHomeAdapter(item.items.take(5))
            }
            binding.genresExpand.setOnClickListener {
                binding.tracksExpanded = !(binding.tracksExpanded!!)
                binding.adapter = PopularityHomeAdapter(
                    if (binding.tracksExpanded!!) {
                        item.items
                    } else {
                        item.items.take(5)
                    }
                )
            }
            binding.executePendingBindings()
        }
    }

    inner class AppContentsViewHolder(val binding: SectionHomeAppcontentsBinding, val viewListener: ViewListener) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppContentsSection){
            val viewPager2 = binding.viewPager
            val dotsIndicator = binding.dots
            val viewPagerAdapter = PagesHomeAdapter(item.items, homeFragment)
            viewPager2.adapter = viewPagerAdapter
            dotsIndicator.attachTo(viewPager2)
            viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {})
            binding.executePendingBindings()
        }
    }

    inner class WordCloudsViewHolder(val binding: SectionHomeWordcloudBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetJavaScriptEnabled")
        fun bind(item: WordCloudsSection){
            val viewPager2 = binding.viewPager
            val dotsIndicator = binding.dots
            val viewPagerAdapter = WorldCloudViewPagerAdapter(item.items)
            viewPager2.adapter = viewPagerAdapter
            dotsIndicator.attachTo(viewPager2)
            viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {})
            binding.executePendingBindings()
        }
    }

    inner class UserStatisticsViewHolder(val binding: SectionHomeStatisticsBinding) : RecyclerView.ViewHolder(binding.root), AdapterView.OnItemSelectedListener {
        private lateinit var selectedItem : HomeStatsSection
        var current = 0
        fun bind(item: HomeStatsSection) {
            selectedItem = item
            binding.title = item.title
            binding.genresExpanded = false
            binding.infoLayout.setOnClickListener {
                val dialog = HelpDialogFragment(item.items[current].help)
                val fragmentManager = homeFragment.requireActivity().supportFragmentManager
                dialog.show(fragmentManager, "help")
            }
            val spinner: Spinner = binding.statsSpinner
            ArrayAdapter(context, R.layout.homepage_stats_spinner_dropdown_title, item.items.map { it.title })
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = this
                }

            binding.genresExpand.setOnClickListener {
                binding.genresExpanded = !(binding.genresExpanded!!)
                binding.adapter = TopGenresHomeAdapter(
                    if (binding.genresExpanded!!) {
                        item.items[current].items.take(15)
                    } else {
                        item.items[current].items.take(5)
                    },
                    clickHandler
                )
            }
            binding.executePendingBindings()
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            binding.adapter = TopGenresHomeAdapter((selectedItem.items[position].items.take(5)), clickHandler)
            binding.genresExpanded = false
            current = position
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }
}