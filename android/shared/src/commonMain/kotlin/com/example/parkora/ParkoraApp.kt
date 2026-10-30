package com.example.parkora

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.example.parkora.navigation.ParkoraNavGraph
import com.example.parkora.ui.components.ToastHost
import com.example.parkora.ui.theme.ParkoraTheme

/*
 * The whole Parkora app as ONE composable.
 * Android shows it from MainActivity, the iPhone app from MainViewController (iosMain).
 */
@Composable
fun ParkoraApp() {
    ParkoraTheme {
        Box {
            ParkoraNavGraph()
            ToastHost()   // short messages such as "Booking approved" (see Toast.kt)
        }
    }
}
