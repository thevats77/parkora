package com.example.parkora.platform

import com.example.parkora.data.FirebaseConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Google sign-in on iPhone is done in Swift with Google's "GoogleSignIn" SDK
 * (iosApp/iosApp/GoogleSignInBridge.swift). Swift gives Kotlin an object with this
 * function at start-up; Kotlin calls it when the user taps "Continue with Google".
 *
 * onResult(idToken, accessToken, errorMessage): exactly one of idToken / errorMessage is set.
 */
interface IosGoogleSignIn {
    fun signIn(onResult: (idToken: String?, accessToken: String?, errorMessage: String?) -> Unit)
    fun signOut()
}

/** iPhone version of the platform bridge (see commonMain/platform/Platform.kt). */
actual object Platform {

    /** Set by Swift (iOSApp.swift) before the first screen is shown. */
    var googleSignIn: IosGoogleSignIn? = null

    actual val name: String = "iOS"

    actual val appContext: Any? = null

    actual val firebaseApiKey: String = FirebaseConfig.IOS_API_KEY

    actual val firebaseAppId: String = FirebaseConfig.IOS_APP_ID

    actual suspend fun signInWithGoogle(): GoogleTokens {
        val bridge = googleSignIn ?: throw GoogleSignInException("Google sign-in is not available.")
        return suspendCancellableCoroutine { cont ->
            bridge.signIn { idToken, accessToken, error ->
                if (idToken != null) cont.resume(GoogleTokens(idToken, accessToken))
                else cont.resumeWithException(GoogleSignInException(error ?: "Sign-in was cancelled."))
            }
        }
    }

    actual suspend fun clearGoogleSignIn() {
        googleSignIn?.signOut()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun compressToJpeg(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? {
        val image = UIImage.imageWithData(bytes.toNSData()) ?: return null
        val (width, height) = image.size.useContents { width to height }
        val scale = minOf(1.0, maxSize / maxOf(width, height))
        val newWidth = width * scale
        val newHeight = height * scale
        // Draw the photo into a smaller canvas, then save it as JPEG
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(newWidth, newHeight))
        val small = renderer.imageWithActions { _ -> image.drawInRect(CGRectMake(0.0, 0.0, newWidth, newHeight)) }
        return UIImageJPEGRepresentation(small, quality / 100.0)?.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(length.toInt())
    if (result.isNotEmpty()) result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return result
}
