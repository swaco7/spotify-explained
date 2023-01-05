package com.example.spotifyexplained.ui.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.SimilarTracksAdapter
import com.example.spotifyexplained.adapter.SimilarTracksByAttributeAdapter
import com.example.spotifyexplained.databinding.FragmentRecommendTrackDetailPageBinding
import com.example.spotifyexplained.general.App
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.html.AudioPage
import com.example.spotifyexplained.html.HorizontalBarChart
import com.example.spotifyexplained.html.RadarChart
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import com.example.spotifyexplained.general.TrackViewModelFactory
import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.google.android.material.snackbar.Snackbar
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Capabilities
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

/**
 * Fragment dedicated to the detail page of the track
 */
class TrackDetailPageFragment : Fragment(), TrackDetailClickHandler, AdapterView.OnItemSelectedListener {
    private lateinit var viewModel: TrackDetailPageViewModel
    private lateinit var webView : WebView
    private lateinit var previewWebView : WebView
    private lateinit var chartWebView : WebView
    private lateinit var trackProgressBar : TrackProgressBar
    private lateinit var connectButton : AppCompatImageButton
    private lateinit var saveButton : Button
    private var _binding: FragmentRecommendTrackDetailPageBinding? = null
    private val binding get() = _binding!!
    private var chosenIndex : Int = 0
    private var position: Int = 0
    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var capabilitiesSubscription: Subscription<Capabilities>? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val errorCallback = { _: Throwable -> logError() }
    private val args: TrackDetailPageFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(this, TrackViewModelFactory(context as MainActivity, args.track, ((context as MainActivity).application as App).repository))[TrackDetailPageViewModel::class.java]
        _binding = FragmentRecommendTrackDetailPageBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        webView = binding.webView
        chartWebView = binding.webViewBar
        previewWebView = binding.webViewPreview

        // Similar tracks overall list
        val similarTracksList: RecyclerView = binding.similarTracksList
        val tracksAdapter = SimilarTracksAdapter(viewModel.similarTracks.value!!, this)
        similarTracksList.adapter = tracksAdapter
        similarTracksList.layoutManager = LinearLayoutManager(context)
        viewModel.similarTracks.observe(viewLifecycleOwner, tracksAdapter::updateData)

        // Similar tracks by attribute list
        val similarTracksByAttribute : RecyclerView = binding.trackByAttributeList
        val tracksByAttrAdapter = SimilarTracksByAttributeAdapter(viewModel.mostSimilarByAttribute.value!!, this)
        similarTracksByAttribute.adapter = tracksByAttrAdapter
        similarTracksByAttribute.layoutManager = LinearLayoutManager(context)
        viewModel.mostSimilarByAttribute.observe(viewLifecycleOwner, tracksByAttrAdapter::updateData)

        viewModel.radarData.observe(viewLifecycleOwner) {
            if (it.tracksWithFeatures.isNotEmpty()) {
                val currentTrack = viewModel.radarData.value!!.tracksWithFeatures[0]
                viewModel.currentTrackTitle.value = "${currentTrack.track.trackName} - ${currentTrack.track.artists[0].artistName}"
                drawD3Graph(viewModel.radarData.value!!)
                if (viewModel.recommendedTrack.value!!.preview_url != null) {
                    showPreview(viewModel.recommendedTrack.value!!)
                }
            }
        }
        viewModel.recommendedTrack.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.currentTrackUri.value = it.uri ?: ""
            }
        }
        viewModel.barData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                drawD3GraphBar(viewModel.barData.value!!)
            }
        }
        binding.infoHeader.setOnClickListener {
            val dialog = HelpDialogFragment(resources.getString(R.string.track_detail_most_similar_overall))
            val fragmentManager = requireActivity().supportFragmentManager
            dialog.show(fragmentManager, "help")
        }
        binding.infoSwitch.setOnClickListener {
            val dialog = HelpDialogFragment(requireActivity().resources.getString(R.string.track_detail_attribute_similarity_info))
            val fragmentManager = requireActivity().supportFragmentManager
            dialog.show(fragmentManager, "help")
        }
        binding.relativeSwitch.setOnCheckedChangeListener { _, checked ->
            viewModel.valueSwitchChanged(checked, this.position)
            binding.relativeSwitch.text = if (checked) getString(R.string.relative) else getString(R.string.absolute)
        }

        val items = AudioFeatureType.values().toList()
        val spinner: Spinner = binding.featuresSpinner
        ArrayAdapter(requireContext(), R.layout.spinner_dropdown_title, items)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = this
            }

        // Spotify playback
        connect(true)
        connectButton = binding.connectPlayButton
        connectButton.setOnClickListener {
            playUri(viewModel.recommendedTrack.value!!.uri!!)
        }
        saveButton = binding.saveRemoveButton
        saveButton.setOnClickListener {
            if (viewModel.isSaved.value!!) onRemoveUriClicked() else onSaveUriClicked()
        }
        binding.playPauseButton.setOnClickListener {
            assertAppRemoteConnected().let {
                it.playerApi
                    .playerState
                    .setResultCallback { playerState ->
                        if (playerState.isPaused) {
                            it.playerApi
                                .resume()
                                .setResultCallback {}
                                .setErrorCallback(errorCallback)
                        } else {
                            it.playerApi
                                .pause()
                                .setResultCallback {}
                                .setErrorCallback(errorCallback)
                        }
                    }
            }
        }
        return binding.root
    }

    override fun onDestroy() {
        try {
            assertAppRemoteConnected().let {
                it.playerApi
                    .playerState
                    .setResultCallback { playerState ->
                        if (!playerState.isPaused) {
                            it.playerApi
                                .pause()
                                .setResultCallback {}
                                .setErrorCallback(errorCallback)
                        }
                    }
            }
        } catch (e : SpotifyDisconnectedException){

        }
        //SpotifyAppRemote.disconnect(spotifyAppRemote)
        super.onDestroy()
        previewWebView.destroy()

    }

    /**
     * Connects to SpotifyAppRemote
     * @param [showAuthView] flag if authentication dialog should be presented
     */
    fun connect(showAuthView: Boolean) {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(showAuthView)
                onSubscribeToCapabilitiesClicked()
                onGetCollectionStateClicked()
            } catch (error: Throwable) {
                //onDisconnected()
            }
        }
    }

    /**
     * Returns connection to SpotifyAppRemote
     */
    private suspend fun connectToAppRemote(showAuthView: Boolean): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                (context as MainActivity).application,
                ConnectionParams.Builder(Config.CLIENT_ID)
                    .setRedirectUri(Config.REDIRECT_URI)
                    .showAuthView(showAuthView)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        cont.resume(spotifyAppRemote)
                    }
                    override fun onFailure(error: Throwable) {}
                })
        }

    /**
     * Plays track based on provided uri
     * @param [uri] of the track to play
     */
    private fun playUri(uri: String) {
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback {
                onSubscribedToPlayerStateButtonClicked()
            }
            .setErrorCallback(errorCallback)
    }

    /**
     * Seeks to position
     * @param [seekToPosition] position in milliseconds
     */
    private fun seekTo(seekToPosition: Long) {
        assertAppRemoteConnected()
            .playerApi
            .seekTo(seekToPosition)
            .setErrorCallback(errorCallback)
    }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->
        updatePlayPauseButton(playerState)
        updateSeekbar(playerState)
    }

    /**
     * Updates control button
     * @param [playerState] current PlayerState
     */
    private fun updatePlayPauseButton(playerState: PlayerState) {
        if (playerState.isPaused) {
            binding.playPauseButton.setImageResource(R.drawable.play_arrow_48px)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.pause_48px)
        }
    }

    /**
     * Stops the player, calls onSkipPreviousButtonClicked to skip to the start of current track
     */
    private fun endOfTrack(){
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (!playerState.isPaused) {
                        it.playerApi
                            .pause()
                            .setResultCallback {onSkipPreviousButtonClicked()}
                            .setErrorCallback(errorCallback)
                    }
                }
        }
    }

    /**
     * Skips to the start of current track
     */
    private fun onSkipPreviousButtonClicked() {
        assertAppRemoteConnected()
            .playerApi
            .skipPrevious()
            .setResultCallback { Log.e("skip", "skipPrevious") }
            .setErrorCallback(errorCallback)
    }

    /**
     * Updates seekbar component based on the current PlayerState
     * @param [playerState] current PlayerState
     */
    private fun updateSeekbar(playerState: PlayerState) {
        trackProgressBar.apply {
            if (playerState.playbackSpeed > 0) {
                unpause()
            } else {
                pause()
            }
            if (connectButton.visibility == View.VISIBLE){
                connectButton.visibility = View.GONE
                return
            }
            binding.seekTo.max = playerState.track.duration.toInt()
            binding.seekTo.isEnabled = true
            setDuration(playerState.track.duration)
            update(playerState.playbackPosition)
        }
    }

    /**
     * Asserts connection to the SpotifyAppRemote
     */
    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        throw SpotifyDisconnectedException()
    }

    private fun logError() {
        //Toast.makeText(context, R.string.err_generic_toast, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackBar(message: String){
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackBar.view.translationY = -60 * (resources.displayMetrics.densityDpi / 160).toFloat()
        snackBar.show()
    }

    /**
     * Subscribes to PlayerState
     */
    private fun onSubscribedToPlayerStateButtonClicked() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)
        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
            .setErrorCallback {
            } as Subscription<PlayerState>
    }

    /**
     * Subscribes to capabilities, for example if user has Spotify premium or not
     */
    private fun onSubscribeToCapabilitiesClicked() {
        capabilitiesSubscription = cancelAndResetSubscription(capabilitiesSubscription)
        capabilitiesSubscription = assertAppRemoteConnected()
            .userApi
            .subscribeToCapabilities()
            .setEventCallback { capabilities ->
                trackProgressBar = TrackProgressBar(binding.seekTo, { seekToPosition: Long -> seekTo(seekToPosition)},{ endOfTrack()})
                viewModel.isPremium.value = capabilities.canPlayOnDemand
            }
            .setErrorCallback(errorCallback) as Subscription<Capabilities>
        assertAppRemoteConnected()
            .userApi
            .capabilities
            .setResultCallback {}
            .setErrorCallback(errorCallback)
    }

    /**
     * Checks if current track is in the user's library
     */
    private fun onGetCollectionStateClicked() {
        assertAppRemoteConnected()
            .userApi
            .getLibraryState(viewModel.currentTrackUri.value!!)
            .setResultCallback { libraryState ->
                viewModel.isSaved.value = !(/*!libraryState.isAdded && */libraryState.canAdd)
            }
            .setErrorCallback { throwable -> logError() }
    }

    /**
     * Removes current track from the user's library
     */
    private fun onRemoveUriClicked() {
        assertAppRemoteConnected()
            .userApi
            .removeFromLibrary(viewModel.currentTrackUri.value!!)
            .setResultCallback { viewModel.isSaved.value = false; showSnackBar(getString(R.string.track_removed_message))}
            .setErrorCallback { throwable -> logError() }
    }

    /**
     * Saves current track to the user's library
     */
    private fun onSaveUriClicked() {
        assertAppRemoteConnected()
            .userApi
            .addToLibrary(viewModel.currentTrackUri.value!!)
            .setResultCallback { viewModel.isSaved.value = true; showSnackBar(getString(R.string.track_added_message))}
            .setErrorCallback { throwable -> logError() }
    }

    /**
     * Resets subscription
     */
    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }

    /**
     * Navigates to track detail page
     * @param [trackName] selected track object
     */
    override fun onTrackClick(trackName: String?) {
        val index = viewModel.radarData.value!!.tracksWithFeatures.indexOfFirst { it.track.trackName == trackName }
        when {
            index != -1 -> {
                chosenIndex = index
                val currentTrack = viewModel.radarData.value!!.tracksWithFeatures[index]
                viewModel.currentTrackTitle.value = "${currentTrack.track.trackName} - ${currentTrack.track.artists[0].artistName}"
            }
            trackName == resources.getString(R.string.average_of_yours) -> {
                chosenIndex = 6
                viewModel.currentTrackTitle.value = resources.getString(R.string.average_of_yours)
            }
            else -> {
                chosenIndex = 7
                viewModel.currentTrackTitle.value = resources.getString(R.string.average_general)
            }
        }
        Log.e("choseIndex", chosenIndex.toString())
        drawD3Graph(viewModel.radarData.value!!)
    }

    override fun onSimilarTrackClick(trackId: String?) {
        val action =
            TrackDetailPageFragmentDirections.actionNavigationTrackDetailToFragmentRecommendTrackDetailPage(
                trackId!!
            )
        NavHostFragment.findNavController(this).navigate(action)
    }


    /**
     *  Loads chart html into the webView.
     *  @param [data] radar chart data.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun drawD3Graph(data: RadarChartData) {
        val currentRadarData = data.copy()
        val colorsAll = mutableListOf("#177ADF", "#2CBA3D", "#2CB9BA", "#2C49BA", "#752DB4", "#CC333F", "#FFDB58", "#FFA500")
        val colors = (colorsAll.take(Config.detailAllCount + 1) + colorsAll.takeLast(2)).toMutableList()
        if (chosenIndex < Config.detailAllCount + 1) {
            val currentData = currentRadarData.tracksWithFeatures[chosenIndex]
            val currentColor = colors[chosenIndex]
            val currentList = currentRadarData.tracksWithFeatures.toMutableList()
            currentList.remove(currentList[chosenIndex])
            colors.removeAt(chosenIndex)
            currentList.add(currentData)
            colors.add(Config.detailAllCount + 2, currentColor)
            currentRadarData.tracksWithFeatures = currentList
        } else {
            if (chosenIndex == Config.detailAllCount + 1){
                val currentColor = colors[chosenIndex]
                colors.removeAt(Config.detailAllCount + 1)
                colors.add(Config.detailAllCount + 2, currentColor)
            }
        }
        currentRadarData.chosenIndex = chosenIndex
        webView.settings.javaScriptEnabled = true
        val displayMetrics = resources.displayMetrics
        val rawHtml = RadarChart.getHeader() +
                RadarChart.getBody() +
                RadarChart.getChartDesignHeader(
                    width = (displayMetrics.widthPixels / displayMetrics.density).toInt() - 100,
                    selected = Config.detailAllCount + 2,
                    selectedTitle = max(Config.detailAllCount,chosenIndex),
                    color = colors
                ) +
                "var data = ${currentRadarData}; \n" +
                "var tracks = ${currentRadarData.tracksToString()}; \n" +
                RadarChart.getChartDesignFooter() +
                RadarChart.getFooter()
        val encodedHtml = Base64.getEncoder().encodeToString(rawHtml.toByteArray())
        webView.loadData(encodedHtml, "text/html", "base64")
    }

    /**
     * Loads content for 30 second long track preview
     * @param [track] track to show preview for
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun showPreview(track: Track) {
        previewWebView.settings.javaScriptEnabled = true
        val displayMetrics = resources.displayMetrics
        val rawHtml = AudioPage.getContent(track.preview_url!!, (displayMetrics.widthPixels / displayMetrics.density).toInt() - 100)
        val encodedHtml = Base64.getEncoder().encodeToString(rawHtml.toByteArray())
        previewWebView.loadData(encodedHtml, "text/html", "base64")
    }

    /**
     *  Loads horizontal bar chart html into the webView.
     *  @param [data] list of data units.
     */
    private fun drawD3GraphBar(data: List<DataUnit>) {
        chartWebView.settings.javaScriptEnabled = true
        val displayMetrics = resources.displayMetrics
        val rawHtml = HorizontalBarChart.getHeader() +
                "var data = $data; \n" +
                HorizontalBarChart.getBody((displayMetrics.widthPixels / displayMetrics.density).toInt() - 10, 280) +
                HorizontalBarChart.getFooter()
        val encodedHtml = Base64.getEncoder().encodeToString(rawHtml.toByteArray())
        chartWebView.loadData(encodedHtml, "text/html", "base64")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.findMostSimilarByAttribute(position)
        this.position = position
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}