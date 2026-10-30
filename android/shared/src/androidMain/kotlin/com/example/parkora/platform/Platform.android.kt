package com.example.parkora.platform

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.parkora.data.FirebaseConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

/** Android version of the platform bridge (see commonMain/platform/Platform.kt). */
actual object Platform {

    private lateinit var context: Context
    private var activity = WeakReference<Activity>(null)

    /** Called by MainActivity.onCreate(). */
    fun attach(activity: Activity) {
        context = activity.applicationContext
        this.activity = WeakReference(activity)
    }

    actual val name: String = "Android"

    actual val appContext: Any? get() = context

    actual val firebaseApiKey: String = FirebaseConfig.API_KEY

    actual val firebaseAppId: String = FirebaseConfig.ANDROID_APP_ID

    /** Shows Android's "Sign in with Google" sheet (Credential Manager). */
    actual suspend fun signInWithGoogle(): GoogleTokens {
        val activity = activity.get() ?: throw GoogleSignInException("Please try again.")
        try {
            val googleOption = GetSignInWithGoogleOption.Builder(FirebaseConfig.WEB_CLIENT_ID).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(googleOption).build()
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) throw GoogleSignInException("Google sign-in returned an unexpected result.")
            return GoogleTokens(GoogleIdTokenCredential.createFrom(credential.data).idToken, null)
        } catch (e: GetCredentialCancellationException) {
            throw GoogleSignInException("Sign-in was cancelled.")
        } catch (e: NoCredentialException) {
            throw GoogleSignInException("No Google account found on this phone. Add one in Settings → Accounts and try again.")
        }
    }

    actual suspend fun clearGoogleSignIn() {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }

    actual fun compressToJpeg(bytes: ByteArray, maxSize: Int, quality: Int): ByteArray? {
        // 1. Read only the picture size first, to choose a cheap down-sampling factor
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) sample *= 2

        // 2. Decode the (already smaller) picture and scale it to at most maxSize px
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null
        val scale = minOf(1f, maxSize.toFloat() / maxOf(decoded.width, decoded.height))
        val bitmap = Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)

        // 3. Compress to JPEG
        return ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }.toByteArray()
    }
}
