package com.example.parkora.data

/*
 * FIREBASE SETTINGS
 * -----------------
 * These values come from the Firebase project "parkora-muj-app"
 * (Firebase console → Project settings → Your apps).
 * They identify the project; they are NOT secret passwords. Security comes from the
 * Firestore Security Rules (firebase/firestore.rules), which run on Google's servers.
 *
 * The same project is used by the Android app, the iPhone app and the website, so both share one live database.
 */
object FirebaseConfig {
    const val PROJECT_ID = "parkora-muj-app"
    const val API_KEY = "AIzaSyAhFdyS-f832gMrA1R3PLD86W6Mg3Kowfg"
    const val SENDER_ID = "902881672311"
    const val ANDROID_APP_ID = "1:902881672311:android:514323728bb556a2039a72"  // Android app in.parkora.app
    const val IOS_APP_ID = "1:902881672311:ios:ddb890a924639326039a72"     // iPhone app in.parkora.app
    const val IOS_API_KEY = "AIzaSyCo_F0_5beMMPYlwAVyhZCDGTUvATBLmdQ"
    /** iOS OAuth client (Google sign-in on iPhone). Also in iosApp/iosApp/Info.plist. */
    const val IOS_CLIENT_ID = "902881672311-47dt5dbdfbcpuiq5v64o0aiudcokc4ak.apps.googleusercontent.com"

    /** OAuth "Web client" id – Google Sign-In needs it to give us an ID token for Firebase. */
    const val WEB_CLIENT_ID = "902881672311-mji4ottsep4p2p0savosdc0nn6cbmbs7.apps.googleusercontent.com"

    /** The Google account that gets the Admin Portal. Must match firebase/firestore.rules. */
    const val ADMIN_EMAIL = "vatsalsharma849@gmail.com"
}
