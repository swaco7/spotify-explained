package com.example.spotifyexplained.ui.playlist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.TracksRecommendedBaseDatabaseAdapter
import com.example.spotifyexplained.database.PlaylistTrackEntity
import com.example.spotifyexplained.databinding.FragmentPlaylistBinding
import com.example.spotifyexplained.general.App
import com.example.spotifyexplained.model.Track
import com.example.spotifyexplained.general.TrackDetailClickHandler
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.model.enums.TrackFeedbackType
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.AudioPage
import com.example.spotifyexplained.ui.detail.TrackProgressBar
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import com.example.spotifyexplained.general.TrackDatabaseViewModelFactory
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

/**
 * Fragment containing playlist functionality
 */
class PlaylistFragment : Fragment(), TrackDetailClickHandler{
    private lateinit var viewModel: PlaylistViewModel
    lateinit var previewWebView : WebView
    private lateinit var adapter: TracksRecommendedBaseDatabaseAdapter<PlaylistTrackEntity>
    private var _binding: FragmentPlaylistBinding? = null
    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var capabilitiesSubscription: Subscription<Capabilities>? = null
    private lateinit var trackProgressBar : TrackProgressBar
    private lateinit var connectButton : AppCompatImageButton
    var spotifyAppRemote: SpotifyAppRemote? = null
    private val errorCallback = { _: Throwable -> logError() }

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            TrackDatabaseViewModelFactory(
                context as MainActivity,
                ((context as MainActivity).application as App).repository
            )
        )[PlaylistViewModel::class.java]
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.menuButton.setOnClickListener {
            val dialog = HelpDialogFragment(resources.getString(R.string.playlist_info_text))
            val fragmentManager = requireActivity().supportFragmentManager
            dialog.show(fragmentManager, "help")
        }
        val deleteButton : Button = binding.deleteButton
        val savePlaylistButton : Button = binding.savePlaylistButton
        val userTracksList: RecyclerView = binding.userTracksList

        previewWebView = binding.webViewPreview
        adapter = TracksRecommendedBaseDatabaseAdapter(this)
        userTracksList.adapter = adapter
        viewModel.playlistTracksLiveData.observe(viewLifecycleOwner) { tracks ->
            tracks.let { submitTracks -> adapter.submitList(submitTracks.filter { it.trackType == TrackFeedbackType.POSITIVE }) }
        }
        connect(true)
        connectButton = binding.connectPlayButton
        connectButton.setOnClickListener {
            playUri(viewModel.currentTrackUri.value!!)
        }
        val saveButton = binding.saveRemoveButton
        saveButton.setOnClickListener {
            if (viewModel.isSaved.value!!) onRemoveUriClicked() else onSaveUriClicked()
        }
        viewModel.nextTrack.observe(viewLifecycleOwner){
            if (it != null){
                showPreview(it.track)
                if (viewModel.currentTrackUri.value!! == "") {
                    viewModel.currentTrackUri.value = it.track.uri ?: ""
                } else {
                    viewModel.currentTrackUri.value = it.track.uri ?: ""
                    if (viewModel.isPremium.value!!) {
                        playWithConnect()
                    }
                }
            }
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
        deleteButton.setOnClickListener {
            deletePlaylist(this.requireContext())
        }
        savePlaylistButton.setOnClickListener {
            createPlaylistDialog()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        _binding = null
    }

    override fun onTrackClick(track: Track?) {
        return
    }

    override fun onTrackLongClick(track: Track?) {
        deleteTrack(this.requireContext(), track!!)
    }

    /**
     * Shows dialog for deleting playlist
     */
    private fun deletePlaylist(context: Context) {
        AlertDialog.Builder(context)
            .setMessage(getString(R.string.delete_playlist_question))
            .setPositiveButton(getString(R.string.cancel)) { p0, _ -> p0.cancel() }
            .setNegativeButton(getString(R.string.delete)) { _, _ -> viewModel.deletePlaylist() }
            .show()
    }

    /**
     * Shows dialog for deleting track
     * @param [track] track to delete from the list
     */
    private fun deleteTrack(context: Context, track: Track ) {
        AlertDialog.Builder(context)
            .setMessage(getString(R.string.delete_track_question))
            .setPositiveButton(getString(R.string.cancel)) { p0, _ -> p0.cancel() }
            .setNegativeButton(getString(R.string.delete)) { _, _ -> viewModel.deleteTrack(viewModel.playlistTracksLiveData.value!!.firstOrNull { it.track == track }!!) }
            .show()
    }

    /**
     * Shows dialog for creating new Spotify playlist
     */
    private fun createPlaylistDialog(){
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater
        val dialogLayout = inflater.inflate(R.layout.create_playlist_dialog, null)
        val nameEditText = dialogLayout.findViewById<EditText>(R.id.playlistName)
        val description = dialogLayout.findViewById<EditText>(R.id.description)
        val public = dialogLayout.findViewById<CheckBox>(R.id.isPublic)
        val collaborative = dialogLayout.findViewById<CheckBox>(R.id.isCollaborative)
        public.setOnClickListener {
            if (public.isChecked) {
                collaborative.isChecked = false
            }
        }
        collaborative.setOnClickListener {
            if (collaborative.isChecked){
                public.isChecked = false
            }
        }
        builder.setView(dialogLayout)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                if (nameEditText.text.isNotEmpty()) {
                    lifecycleScope.launch {
                        val playlist = ApiHelper.createPlaylist(
                            nameEditText.text.toString(),
                            description.text.toString(),
                            public.isChecked,
                            collaborative.isChecked,
                            context!!
                        )
                        val snapshot = ApiHelper.addTracksToPlaylist(
                            playlist!!.id,
                            viewModel.playlistTracksLiveData.value!!.filter { it.trackType == TrackFeedbackType.POSITIVE }
                                .map { it.track }.toMutableList(),
                            context!!
                        )?.snapshot_id ?: ""
                        showSnackBar("Playlist ${nameEditText.text} created")
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    /**
     * Loads content for 30 second long track preview
     * @param [track] track to show preview for
     */
    private fun showPreview(track: Track) {
        previewWebView.settings.javaScriptEnabled = true
        val displayMetrics = resources.displayMetrics
        val rawHtml = AudioPage.getContent(track.preview_url ?: "", (displayMetrics.widthPixels / displayMetrics.density).toInt() - 100)
        val encodedHtml = Base64.getEncoder().encodeToString(rawHtml.toByteArray())
        previewWebView.loadData(encodedHtml, "text/html", "base64")
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
     * Checks if user is connected to the remote, if not it connects.
     * Plays track based on the current uri
     */
    private fun playWithConnect() {
        if (spotifyAppRemote?.isConnected == true) {
            playUri(viewModel.currentTrackUri.value!!)
        } else {
            connect(true)
            playUri(viewModel.currentTrackUri.value!!)
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
                viewModel.isSaved.value = !(libraryState.canAdd)
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
}