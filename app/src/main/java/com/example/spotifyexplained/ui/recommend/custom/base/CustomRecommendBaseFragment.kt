package com.example.spotifyexplained.ui.recommend.custom.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.databinding.FragmentRecommendBaseBinding
import com.example.spotifyexplained.general.App
import com.example.spotifyexplained.general.GestureListener
import com.example.spotifyexplained.general.TrackDatabaseViewModelFactory
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.ui.recommend.custom.overall.CustomRecommendOverallFragment
import com.example.spotifyexplained.ui.recommend.custom.settings.CustomRecommendSettingsFragment
import com.example.spotifyexplained.ui.recommend.custom.specific.CustomRecommendSpecificFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Fragment dedicated to custom recommender system, it collects the pool of possible tracks
 */
class CustomRecommendBaseFragment : Fragment() {
    private lateinit var viewModel: CustomRecommendBaseViewModel
    private var _binding: FragmentRecommendBaseBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this, TrackDatabaseViewModelFactory(
                context as MainActivity,
                ((context as MainActivity).application as App).repository
            )
        )[CustomRecommendBaseViewModel::class.java]
        _binding = FragmentRecommendBaseBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.myToolbar.setNavigationOnClickListener {
            viewModel.expanded.value = !viewModel.expanded.value!!
        }
        val root: View = binding.root
        val tabLayout : TabLayout = binding.topTab
        val viewPager : ViewPager2 = binding.viewPager
        val reloadButton: ImageView = binding.reloadButton
        viewPager.adapter = PagerAdapter()
        viewPager.isUserInputEnabled = false
        val texts = arrayOf(
            getString(R.string.overallTab),
            getString(R.string.mostSimilarTab),
            getString(R.string.customTab)
        )
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = texts[position]
        }.attach()
        viewPager.visibility = View.GONE

        viewModel.loadingState.observe(viewLifecycleOwner) {
            binding.poolLoading = it == LoadingState.LOADING
            (context as MainActivity).viewModel.expanded.value = it == LoadingState.LOADING
        }

        (context as MainActivity).viewModel.loadingProgress.observe(viewLifecycleOwner) {
            viewModel.loadingProgress.value = it
        }

        (context as MainActivity).viewModel.progressText.observe(viewLifecycleOwner) {
            viewModel.progressText.value = it
        }

        (context as MainActivity).viewModel.phase.observe(viewLifecycleOwner) {
            viewModel.phase.value = it
        }

        (context as MainActivity).viewModel.expanded.observe(viewLifecycleOwner) {
            viewModel.expanded.value = it
        }

        (context as MainActivity).viewModel.tabVisible.observe(viewLifecycleOwner) {
            viewModel.tabVisible.value = it
        }

        (context as MainActivity).viewModel.poolIsLoading.observe(viewLifecycleOwner) {
            viewModel.loadingState.value = if (!it) {
                if ((context as MainActivity).viewModel.progressText.value != getString(R.string.loadingPoolStopped)) {
                    LoadingState.SUCCESS
                } else {
                    LoadingState.FAILURE
                }
            } else {
                LoadingState.LOADING
            }
        }

        val expandFunc = { expanded : Boolean -> (context as MainActivity).viewModel.expanded.value = !expanded }

        val mGestureDetector = GestureDetectorCompat(context, GestureListener(expandFunc))

        tabLayout.setOnTouchListener { _, event -> mGestureDetector.onTouchEvent(event)
            true
        }

        reloadButton.setOnClickListener {
            if (!(context as MainActivity).viewModel.poolIsLoading.value!!) {
                viewModel.deleteCaches()
                viewModel.reload(true)
            }
//            else {
//                (context as MainActivity).viewModel.job.cancel()
//                (context as MainActivity).viewModel.progressText.value = getString(R.string.loadingPoolStopped)
//                (context as MainActivity).viewModel.poolIsLoading.value = false
//
//            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class PagerAdapter() : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CustomRecommendOverallFragment()
                1 -> CustomRecommendSpecificFragment()
                else -> CustomRecommendSettingsFragment()
            }
        }
    }
}