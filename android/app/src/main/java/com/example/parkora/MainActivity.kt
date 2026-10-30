package com.example.parkora

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.platform.Platform

/*
 * MAIN ACTIVITY – the entry point of the Android app.
 * ---------------------------------------------------
 * An Activity is one "window" of an Android app. Parkora uses a single
 * Activity; every screen is a @Composable function from the SHARED module
 * (android/shared), which the iPhone app uses too.
 *
 * onCreate() runs when Android starts the app:
 *   1. Draw behind the status bar so the yellow header reaches the top edge.
 *   2. Give the shared code this Activity (needed for the Google sign-in sheet).
 *   3. Connect to Firebase, then show ParkoraApp() (theme + all screens).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dark status-bar icons on our light/yellow screens
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        Platform.attach(this)
        ParkoraRepository.init()

        setContent {
            ParkoraApp()
        }
    }
}
