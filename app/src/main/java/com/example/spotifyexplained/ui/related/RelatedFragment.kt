package com.example.spotifyexplained.ui.related

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.spotifyexplained.databinding.FragmentRecommendBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RelatedFragment : Fragment() {
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager : ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecommendBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val tabLayout: TabLayout = binding.topTab
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = PagerAdapter()
        viewPager.isUserInputEnabled = false
        this.viewPager = viewPager
        val texts = arrayOf("Artists", "Genres")
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = texts[position]
        }.attach()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class PagerAdapter() : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> RelatedArtistsGraphFragment()
                else -> RelatedGenresGraphFragment()
            }
        }
    }
}