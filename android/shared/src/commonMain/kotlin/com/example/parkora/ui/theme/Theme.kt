package com.example.parkora.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/*
 * ParkoraTheme wraps the whole app (see MainActivity).
 * Every Material component inside it (Text, Button, TextField, ...)
 * automatically uses these colours and the Work Sans font.
 * Parkora only has a light (yellow & white) design, like the screenshots.
 */
private val ParkoraColorScheme = lightColorScheme(
    primary = DarkNavy,
    onPrimary = ScreenWhite,
    secondary = ParkoraYellow,
    onSecondary = DarkNavy,
    background = ScreenWhite,
    onBackground = DarkNavy,
    surface = ScreenWhite,
    onSurface = DarkNavy,
    surfaceVariant = CardCream,
    onSurfaceVariant = TextGray,
    error = RejectedRed
)

@Composable
fun ParkoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ParkoraColorScheme,
        typography = parkoraTypography(),
        content = content
    )
}
