package com.example.spotifyexplained.ui.recommend.spotify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.databinding.FragmentRecommendBinding
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import com.example.spotifyexplained.ui.recommend.spotify.artists.ArtistRecommendFragment
import com.example.spotifyexplained.ui.recommend.spotify.combined.CombinedRecommendFragment
import com.example.spotifyexplained.ui.recommend.spotify.genre.GenresRecommendFragment
import com.example.spotifyexplained.ui.recommend.spotify.tracks.TrackRecommendFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Base fragment for recommendations from Spotify.
 */
class RecommendFragment : Fragment() {
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val tabLayout: TabLayout = binding.topTab
        val viewPager: ViewPager2 = binding.viewPager
        binding.menuButton.setOnClickListener {
            val dialog = HelpDialogFragment(resources.getString(R.string.spotify_recommend_info_text))
            val fragmentManager = requireActivity().supportFragmentManager
            dialog.show(fragmentManager, "help")
        }
        viewPager.adapter = PagerAdapter()
        viewPager.isUserInputEnabled = false
        val texts = arrayOf(
            getString(R.string.by_tracks),
            getString(R.string.by_artists),
            getString(R.string.by_genres),
            getString(R.string.combined)
        )
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
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TrackRecommendFragment()
                1 -> ArtistRecommendFragment()
                2 -> GenresRecommendFragment()
                else -> CombinedRecommendFragment()
            }
        }
    }
}