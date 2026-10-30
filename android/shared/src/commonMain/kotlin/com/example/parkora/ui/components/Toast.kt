package com.example.parkora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/*
 * TOAST – a short message at the bottom of the screen ("Booking approved").
 * Android's Toast class does not exist on iPhone, so we draw our own with Compose:
 * showToast("text") sets the message, and ToastHost (placed once over the whole app
 * in ParkoraApp) shows it for 2.5 seconds.
 */
private var toastMessage by mutableStateOf<String?>(null)
private var toastId by mutableStateOf(0)

fun showToast(message: String) {
    toastMessage = message
    toastId++
}

@Composable
fun ToastHost() {
    LaunchedEffect(toastId) {
        delay(2500)
        toastMessage = null
    }
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = toastMessage != null, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = toastMessage ?: "",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp, bottom = 96.dp)
                    .background(Color(0xE6323232), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
