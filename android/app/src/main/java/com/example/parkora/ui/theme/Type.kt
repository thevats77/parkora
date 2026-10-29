package com.example.parkora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.parkora.R

/*
 * The screenshots use the "Work Sans" font (free Google Font, OFL licence).
 * The file res/font/work_sans.ttf is a "variable font": ONE file that can
 * draw every weight. We register the weights we need below.
 */
@OptIn(ExperimentalTextApi::class) // FontVariation is still marked "experimental" in Compose
private fun workSans(weight: FontWeight) = Font(
    resId = R.font.work_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

val WorkSans = FontFamily(
    workSans(FontWeight.Normal),
    workSans(FontWeight.Medium),
    workSans(FontWeight.SemiBold),
    workSans(FontWeight.Bold)
)

/** Material 3 text styles, all using Work Sans. */
val ParkoraTypography = Typography(
    headlineSmall = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Medium, fontSize = 22.sp, color = DarkNavy),
    titleLarge = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    titleSmall = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    labelMedium = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = WorkSans, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)
