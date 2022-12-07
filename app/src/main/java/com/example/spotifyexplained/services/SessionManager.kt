package com.example.spotifyexplained.services

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import com.example.spotifyexplained.R
import com.example.spotifyexplained.general.App


object SessionManager {
    private var prefs: SharedPreferences = App.context.getSharedPreferences(App.context.getString(R.string.app_name), Context.MODE_PRIVATE)
    private const val TOKEN = "token"
    private const val EXPIRES_AT = "expires_at"
    private const val USERID = "userId"

    fun saveToken(token: String, expiresAt: Long){
        val editor = prefs.edit()
        editor.putString(TOKEN, token)
        editor.putLong(EXPIRES_AT, expiresAt)
        editor.apply()
    }

    fun getUserId(): String? {
        return prefs.getString(USERID, null)
    }

    fun saveUserId(userId: String){
        val editor = prefs.edit()
        editor.putString(USERID, userId)
        editor.apply()
    }

    fun isInternetAvailable(): Boolean {
        val connMgr = App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connMgr != null) {
            val activeNetworkInfo = connMgr.activeNetwork
            if (activeNetworkInfo != null) { // connected to the internet
               return true
            }
        }
        return false
    }

    fun fetchToken(): String? {
        return prefs.getString(TOKEN, null)
    }

    fun clearToken() {
        val editor = prefs.edit()
        editor.remove(TOKEN)
        editor.remove(EXPIRES_AT)
        editor.apply()
    }

    fun tokenExpired(): Boolean {
        Log.e("remaining for token", ((prefs.getLong(EXPIRES_AT, 0) - System.currentTimeMillis())/1000).toString())
        return prefs.getLong(EXPIRES_AT, 0) < System.currentTimeMillis()
    }
}