package com.example.spotifyexplained.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.spotifyexplained.R
import com.example.spotifyexplained.databinding.ActivityMainBinding
import com.example.spotifyexplained.general.App
import com.example.spotifyexplained.model.Constants
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import com.example.spotifyexplained.ui.saved.TrackDatabaseViewModelFactory
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
//        viewModel =
//            ViewModelProvider(
//                this,
//                TrackDatabaseViewModelFactory(this, (application as App).repository)
//            )[MainViewModel::class.java]

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_custom_recommend,
                R.id.navigation_top_songs,
                R.id.navigation_recommended_songs,
                //R.id.navigation_related_artists
            )
        )
        navView.setupWithNavController(navController)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun authorizeUser(){
        val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
        AuthorizationClient.openLoginActivity(
            this,
            Constants.AUTH_TOKEN_REQUEST_CODE,
            request
        )
    }

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest? {
        return AuthorizationRequest.Builder(
            Constants.CLIENT_ID,
            type,
            Uri.parse(Constants.REDIRECT_URI).toString()
        )
            .setShowDialog(false)
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
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Constants.AUTH_TOKEN_REQUEST_CODE == requestCode) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            if (response.accessToken != null) {
                lifecycleScope.launch {
                    SessionManager.saveToken(
                        response.accessToken,
                        System.currentTimeMillis() + (response.expiresIn * 1000 - 1000)
                    )
                    val profile = ApiHelper.getProfile()
                    val savedUserId = SessionManager.getUserId()
                    if (savedUserId == null || savedUserId != profile!!.id){
                        logoutUser()
                    }
                    SessionManager.saveUserId(profile!!.id)
                    val id = navController.currentDestination?.id
                    val mArgs = navController.currentBackStackEntry?.arguments
                    navController.popBackStack(id!!, true)
                    if (mArgs != null) {
                        navController.navigate(id, mArgs)
                    } else {
                        navController.navigate(id)
                    }
                }
            } else {
                SessionManager.clearToken()
                authorizeUser()
            }
        }
    }
}