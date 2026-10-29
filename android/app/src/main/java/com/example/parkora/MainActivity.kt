package com.example.parkora

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.navigation.ParkoraNavGraph
import com.example.parkora.ui.theme.ParkoraTheme

/*
 * MAIN ACTIVITY – the entry point of the app.
 * -------------------------------------------
 * An Activity is one "window" of an Android app. Parkora uses a single
 * Activity; every screen inside it is a @Composable function, and
 * ParkoraNavGraph switches between those screens.
 *
 * onCreate() runs when Android starts the app:
 *   1. Draw behind the status bar so the yellow header reaches the top edge.
 *   2. Load saved data (users, spots, bookings) from phone storage.
 *   3. setContent { } shows our Compose UI wrapped in the Parkora theme.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dark status-bar icons on our light/yellow screens
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        ParkoraRepository.init(applicationContext)

        setContent {
            ParkoraTheme {
                ParkoraNavGraph()
            }
        }
    }
}
