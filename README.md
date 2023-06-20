# SpotifyExplained 

## Installation
Requirements: Android min SDK 26 (https://developer.android.com/studio/releases/platforms)

There is an APK file with the installation in the root directory [apk](https://github.com/swaco7/spotify-explained/blob/main/spotify-explained.apk).

APK can be build directly from Android Studio => Build -> Build Bundle(s) / APK(s) -> Build APK(s), this creates an APK named app-debug.apk in app/build/intermediates/apk/debug.

Alternatively APK can be build from command line => navigate to the root of your project directory -> gradlew assembleDebug

## Spotify
App requires Spotify account. If there is an official Spotify app installed in the device, our application automatically logs in to that current account. 
If Spotify app is not installed user is prompted to log in to webview (https://accounts.spotify.com). To logout it might be necessary to choose “Not you?” link in the dialog.

Account needs to be added to Spotify dashboard, otherwise access to API is forbidden. Please contact the author.

If users do not have access to Spotify account or the account is empty, we prepared test account, it is a google account so the google account option needs to be selected during login -> name: test.spotifyexplained@gmail.com, password: SEtest1234. 
 



