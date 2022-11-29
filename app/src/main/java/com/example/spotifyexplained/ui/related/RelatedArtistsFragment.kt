package com.example.spotifyexplained.ui.related

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.ArtistsWithRelatedAdapter
import com.example.spotifyexplained.databinding.FragmentRelatedArtistsBinding
import com.example.spotifyexplained.ui.saved.ContextViewModelFactory

class RelatedArtistsFragment : Fragment() {
    private lateinit var viewModel: RelatedArtistsViewModel
    private lateinit var adapter: ArtistsWithRelatedAdapter
    private var _binding: FragmentRelatedArtistsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this, ContextViewModelFactory(context as MainActivity))[RelatedArtistsViewModel::class.java]
        _binding = FragmentRelatedArtistsBinding.inflate(inflater, container, false)
        val userTracksList: RecyclerView = binding.topArtistsList
        adapter = ArtistsWithRelatedAdapter(viewModel.artists.value!!)
        userTracksList.adapter = adapter
        viewModel.artists.observe(viewLifecycleOwner, adapter::updateData)
        val mDividerItemDecoration = DividerItemDecoration(
            userTracksList.context,
            LinearLayoutManager.VERTICAL
        )
        userTracksList.addItemDecoration(mDividerItemDecoration)
        return binding.root
    }
}