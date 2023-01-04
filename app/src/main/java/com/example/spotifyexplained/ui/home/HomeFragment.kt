package com.example.spotifyexplained.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.HomePageAdapter
import com.example.spotifyexplained.databinding.FragmentHomeBinding
import com.example.spotifyexplained.general.App
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.services.SessionManager
import com.example.spotifyexplained.general.TrackDatabaseViewModelFactory
import com.faltenreich.skeletonlayout.*
import com.spotify.sdk.android.auth.AuthorizationClient

/**
 * Fragment dedicated to homepage, it is also the default fragment for navigation
 */
class HomeFragment : Fragment(), AdapterView.OnItemSelectedListener, TrackDetailClickHandler {
    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var homePageAdapter : HomePageAdapter
    private val binding get() = _binding!!
    private lateinit var skeleton: Skeleton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(
                this,
                TrackDatabaseViewModelFactory(context as MainActivity, ((context as MainActivity).application as App).repository)
            )[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val root: View = binding.root
        val homeRecyclerView = binding.homeRecyclerView
        homePageAdapter = HomePageAdapter(viewModel.statsItems.value!!, this, this)
        homeRecyclerView.adapter = homePageAdapter
        skeleton = homeRecyclerView.applySkeleton(R.layout.skeleton_home_section)
        skeleton.maskColor = Helper.getSkeletonColor(this.requireContext())
        skeleton.showSkeleton()

        viewModel.loadingState.observe(viewLifecycleOwner) {
            if (it == LoadingState.SUCCESS){
                onDataLoaded()
            }
        }

        viewModel.statsItems.observe(viewLifecycleOwner, homePageAdapter::updateData)
        val menuButton = binding.menuButton
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this.requireActivity(), menuButton)
            popupMenu.menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.logout_item) {
                    (context as MainActivity).logoutUser()
                    SessionManager.clearToken()
                    //AuthorizationClient.clearCookies(this.requireActivity())
                    (context as MainActivity).authorizeUser()
                } else if (menuItem.itemId == R.id.clear_caches){
                    viewModel.clearCaches()
                }
                true
            }
            popupMenu.show()
        }
        return root
    }

    private fun onDataLoaded(){
        skeleton.showOriginal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTrackClick(trackName: String?) {
        val action =
            HomeFragmentDirections.actionNavigationHomeToTrackDetail(
                trackName!!
            )
        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        SessionManager.clearToken()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        //TODO("Not yet implemented")
    }
}