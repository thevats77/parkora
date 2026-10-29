package com.example.parkora.data

/*
 * FIREBASE SETTINGS
 * -----------------
 * These values come from the Firebase project "parkora-muj-app"
 * (Firebase console → Project settings → Your apps).
 * They identify the project; they are NOT secret passwords. Security comes from the
 * Firestore Security Rules (firebase/firestore.rules), which run on Google's servers.
 *
 * The same project is used by the Android app and the website, so both share one live database.
 */
object FirebaseConfig {
    const val PROJECT_ID = "parkora-muj-app"
    const val API_KEY = "AIzaSyAhFdyS-f832gMrA1R3PLD86W6Mg3Kowfg"
    const val ANDROID_APP_ID = "1:902881672311:android:514323728bb556a2039a72"  // app in.parkora.app

    /** OAuth "Web client" id – Google Sign-In needs it to give us an ID token for Firebase. */
    const val WEB_CLIENT_ID = "902881672311-mji4ottsep4p2p0savosdc0nn6cbmbs7.apps.googleusercontent.com"

    /** The Google account that gets the Admin Portal. Must match firebase/firestore.rules. */
    const val ADMIN_EMAIL = "vatsalsharma849@gmail.com"
}
