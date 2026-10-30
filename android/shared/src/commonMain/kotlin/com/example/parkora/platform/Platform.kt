package com.example.parkora.platform

/*
 * PLATFORM BRIDGE
 * ---------------
 * Almost all of Parkora is shared Kotlin code. These few jobs need the phone's own
 * system, so each platform writes its own version ("expect" here, "actual" in
 * androidMain/ and iosMain/):
 *
 *   job                       Android (androidMain)            iPhone (iosMain)
 *   ------------------------  -------------------------------  ---------------------------------
 *   Sign in with Google       Credential Manager sheet         GoogleSignIn SDK (called from Swift)
 *   Make a photo smaller      android.graphics.Bitmap          UIKit UIImage
 *   Firebase app id           Android app id                   iOS app id
 */

/** The two tokens Google gives us after the user picks an account. */
class GoogleTokens(val idToken: String, val accessToken: String?)

/** Thrown with a friendly message, e.g. "Sign-in was cancelled." */
class GoogleSignInException(message: String) : Exception(message)

expect object Platform {
    /** "Android" or "iOS" (shown in the About screen). */
    val name: String

    /** Firebase needs the Android Context; on iPhone this is null. */
    val appContext: Any?

    /** The API key of THIS app (Android and iOS keys are different). */
    val firebaseApiKey: String

    /** The id of THIS app inside the Firebase project (see FirebaseConfig.kt). */
    val firebaseAppId: String

    /** Shows Google's account picker and returns the tokens, or throws GoogleSignInException. */
    suspend fun signInWithGoogle(): GoogleTokens

    /** Forgets the chosen Google account (so the picker shows again after logout). */
    suspend fun clearGoogleSignIn()

    /** Shrinks a photo to at most [maxSize] px and returns it as JPEG bytes (null if it is not a photo). */
    fun compressToJpeg(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray?
}
