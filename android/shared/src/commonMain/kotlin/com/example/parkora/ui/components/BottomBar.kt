package com.example.parkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.ui.theme.CardCream
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.TextGray

/** Which tab of the bottom bar is selected. */
enum class BottomTab { HOME, PROFILE }

/**
 * Bottom navigation bar from the screenshots:
 * "Home" on the left, "Profile" on the right and a round yellow "+" button
 * in the middle that opens the 4-step "List Parking Spot" form.
 */
@Composable
fun ParkoraBottomBar(
    selectedTab: BottomTab,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // The cream bar with rounded top corners
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = CardCream,
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(66.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    label = "Home",
                    icon = if (selectedTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    selected = selectedTab == BottomTab.HOME,
                    onClick = onHomeClick,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(0.8f)) // space under the "+" button
                BottomBarItem(
                    label = "Profile",
                    icon = if (selectedTab == BottomTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    selected = selectedTab == BottomTab.PROFILE,
                    onClick = onProfileClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // The round "+" button: white ring around a yellow circle
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(74.dp)
                .shadow(6.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(6.dp)
                .background(ParkoraYellow, CircleShape)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Add, contentDescription = "List a parking spot", tint = DarkNavy, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = DarkNavy, modifier = Modifier.size(30.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) DarkNavy else TextGray
        )
    }
}
