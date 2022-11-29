package com.example.spotifyexplained.ui.topItems

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.TracksAdapter
import com.example.spotifyexplained.databinding.FragmentTopItemsBinding
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import com.example.spotifyexplained.ui.saved.ContextViewModelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Base fragment for user's top items from Spotify.
 */
class TopItemsFragment : Fragment() {
    private var _binding: FragmentTopItemsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopItemsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val tabLayout: TabLayout = binding.topTab
        val viewPager: ViewPager2 = binding.viewPager
        binding.menuButton.setOnClickListener {
            val dialog = HelpDialogFragment(resources.getString(R.string.top_info_text))
            val fragmentManager = requireActivity().supportFragmentManager
            dialog.show(fragmentManager, "help")
        }
        viewPager.adapter = PagerAdapter()
        viewPager.isUserInputEnabled = false
        val texts = arrayOf(
            getString(R.string.topTracksTabTitle),
            getString(R.string.topArtistsTabTitle),
            getString(R.string.saveTracksTabTitle)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = texts[position]
        }.attach()
        return root
    }

    private inner class PagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TopTracksFragment()
                1 -> TopArtistsFragment()
                else -> SavedSongsFragment()
            }
        }
    }
}