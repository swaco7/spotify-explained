package com.example.spotifyexplained.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.spotifyexplained.R
import com.example.spotifyexplained.databinding.ActivityMainBinding
import com.example.spotifyexplained.general.App
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.services.ApiRepository
import com.example.spotifyexplained.services.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var  navController: NavController
    val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        viewModel.connectionError.observe(this){
            if (it && !viewModel.authenticationPending.value!!) {
                showError(getString(R.string.connection_dialog_message))
            }
        }
        viewModel.accessError.observe(this){
            if (it) {
                showForbiddenError(getString(R.string.forbidden))
            }
        }
        navView.setupWithNavController(navController)
    }

    private fun showError(error: String ) {
        android.app.AlertDialog.Builder(this)
            .setMessage(error)
            .setPositiveButton(
                getString(R.string.connection_dialog_close_button)
            ) { p0, _ ->
                p0.cancel()
                viewModel.connectionError.value = false
                refreshCurrentDestination()
            }
            .setCancelable(false)
            .show()
    }

    private fun showForbiddenError(error: String ) {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.something_wrong))
            .setMessage(error)
            .setPositiveButton(
                getString(R.string.connection_dialog_close_button)
            ) { p0, _ ->
                p0.cancel()
                viewModel.accessError.value = false
                refreshCurrentDestination()
            }
            .setNegativeButton(
                getString(R.string.logout_button)
            ){ p0, _ ->
                p0.cancel()
                viewModel.accessError.value = false
                logoutUser()
                SessionManager.clearToken()
                //AuthorizationClient.clearCookies(this)
                authorizeUser()
            }
            .setCancelable(false)
            .show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun getRedirectUri(): Uri? {
        return Uri.Builder()
            .scheme("spotify-sdk")
            .authority("auth")
            .build()
    }

    fun authorizeUser(){
        if (!viewModel.authenticationPending.value!!) {
            viewModel.authenticationPending.value = true
            val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
            AuthorizationClient.openLoginActivity(
                this,
                Config.AUTH_TOKEN_REQUEST_CODE,
                request
            )
        }
    }

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest? {
        return AuthorizationRequest.Builder(
            Config.CLIENT_ID,
            type,
            getRedirectUri().toString()
        )
            .setShowDialog(true)
            .setScopes(arrayOf("user-read-private", "user-library-read", "user-top-read", "playlist-modify-private", "playlist-modify-public"))
            .setCampaign("your-campaign-token")
            .build()
    }

    fun logoutUser(){
        lifecycleScope.launch {
            (application as App).repository.delete()
            (application as App).repository.deleteTrack()
            (application as App).repository.deleteArtist()
            (application as App).repository.deleteGenre()
            (application as App).repository.deleteCombined()
            (application as App).repository.deletePool()
            (application as App).repository.deleteSpecific()
            (application as App).repository.deleteCustom()
            (application as App).repository.deletePlaylist()
            (application as App).repository.deletePlaylistNext()
            (application as App).repository.deleteUserArtists()
            (application as App).repository.deleteUserTracks()
            (application as App).repository.deleteFeatures()
            (application as App).repository.deleteRandomTracks()
            viewModel.topTracks.value = mutableListOf()
            viewModel.topArtists.value = mutableListOf()
        }
    }

    fun refreshCurrentDestination(){
        val id = navController.currentDestination?.id
        val mArgs = navController.currentBackStackEntry?.arguments
        navController.popBackStack(id!!, true)
        if (mArgs != null) {
            navController.navigate(id, mArgs)
        } else {
            navController.navigate(id)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        viewModel.authenticationPending.value = false
        if (Config.AUTH_TOKEN_REQUEST_CODE == requestCode) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            if (response.accessToken != null) {
                lifecycleScope.launch {
                    SessionManager.saveToken(
                        response.accessToken,
                        System.currentTimeMillis() + (response.expiresIn * 1000 - 1000)
                    )
                    val profile = ApiRepository.getProfile(this@MainActivity)
                    val savedUserId = SessionManager.getUserId()
                    if (savedUserId == null || savedUserId != profile?.id){
                        logoutUser()
                    }
                    SessionManager.saveUserId(profile?.id ?: "")
                    refreshCurrentDestination()
                }
            } else {
                if (SessionManager.isInternetAvailable()) {
                    SessionManager.clearToken()
                    authorizeUser()
                } else {
                    viewModel.connectionError.value = true
                }
            }
        }
    }
}