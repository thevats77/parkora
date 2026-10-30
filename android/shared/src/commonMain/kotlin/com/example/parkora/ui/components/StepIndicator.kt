package com.example.parkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.LightGray

/** The four steps of the "List Parking Spot" form (synopsis, section 9). */
val listingStepTitles = listOf("Location/\nAddress", "Parking\nSize", "Upload\nDocuments", "Pricing")

/**
 * Step indicator: ①━━②───③───④
 * Steps that are done or current are dark, future steps are grey.
 * Each step is a column of equal width; the connecting line is drawn as two
 * halves (left and right of the circle) so everything lines up perfectly.
 */
@Composable
fun StepIndicator(currentStep: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 16.dp)) {
        listingStepTitles.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isActive = stepNumber <= currentStep

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // left half of the line (not drawn for the first step)
                        Box(
                            modifier = Modifier.weight(1f).height(2.dp)
                                .background(if (index == 0) Color.Transparent else if (isActive) DarkNavy else LightGray)
                        )
                        // right half of the line (not drawn for the last step)
                        Box(
                            modifier = Modifier.weight(1f).height(2.dp)
                                .background(if (index == listingStepTitles.lastIndex) Color.Transparent else if (stepNumber < currentStep) DarkNavy else LightGray)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (isActive) DarkNavy else LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$stepNumber", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = title,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center,
                    color = DarkNavy,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
