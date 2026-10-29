package com.example.parkora.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.BottomTab
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.ParkingCard
import com.example.parkora.ui.components.ParkoraBottomBar
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.NotificationRed
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.ParkoraYellowLight
import com.example.parkora.ui.theme.ScreenWhite
import kotlinx.coroutines.launch

/*
 * HOME SCREEN  (Figure 1 – "Home / Latest Listed Parking")
 * ---------------------------------------------------------
 * - Yellow header: "Hey, <name>", the user's location (tap ✏ to change it) and a bell.
 * - "Latest Listed" list of parking cards (newest first).
 * - Bottom bar with Home, "+" and Profile.
 */
@Composable
fun HomeScreen(
    onSpotClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val user = ParkoraRepository.currentUser
    // Reading the repository's state list here means this screen redraws
    // automatically when a new spot is added.
    val latestSpots = ParkoraRepository.latestSpots().take(5)
    var showLocationDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader(
                firstName = user?.name?.substringBefore(" ") ?: "",
                location = user?.location ?: "",
                hasUnread = ParkoraRepository.hasUnreadNotifications(),
                onEditLocation = { showLocationDialog = true },
                onBellClick = onNotificationsClick
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Latest Listed", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = DarkNavy, modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.clickable(onClick = onViewAllClick).padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("View All", fontSize = 15.sp, color = DarkNavy)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Filled.ArrowCircleRight, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                if (ParkoraRepository.isLoadingSpots) {
                    // The first data has not arrived from the server yet
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DarkNavy)
                        }
                    }
                } else if (latestSpots.isEmpty()) {
                    item { EmptyState("No parking spots listed yet.\nTap + to list the first one!") }
                }
                items(latestSpots, key = { it.id }) { spot ->
                    ParkingCard(
                        spot = spot,
                        onClick = { onSpotClick(spot.id) },
                        onBookClick = { onSpotClick(spot.id) }
                    )
                }
            }
        }

        ParkoraBottomBar(
            selectedTab = BottomTab.HOME,
            onHomeClick = {},
            onAddClick = onAddClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showLocationDialog) {
        EditLocationDialog(
            currentLocation = user?.location ?: "",
            onDismiss = { showLocationDialog = false },
            onSave = { newLocation ->
                showLocationDialog = false
                scope.launch { ParkoraRepository.updateLocation(newLocation) }
            }
        )
    }
}

@Composable
private fun HomeHeader(
    firstName: String,
    location: String,
    hasUnread: Boolean,
    onEditLocation: () -> Unit,
    onBellClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(ParkoraYellow)
            .drawBehind {
                drawCircle(ParkoraYellowLight, radius = size.width * 0.42f, center = Offset(size.width * 0.12f, -size.height * 0.2f))
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hey, $firstName", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                Row(
                    modifier = Modifier.clickable(onClick = onEditLocation).padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PersonPin, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(location, fontSize = 15.sp, color = DarkNavy, maxLines = 1)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Filled.Edit, contentDescription = "Change location", tint = DarkNavy, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onBellClick) {
                Box {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = DarkNavy, modifier = Modifier.size(32.dp))
                    if (hasUnread) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .background(NotificationRed, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

/** Simple dialog to type a new location (no GPS – the user types it). */
@Composable
private fun EditLocationDialog(currentLocation: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(currentLocation) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your location") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; error = null },
                    label = { Text("Area, City, Country") },
                    isError = error != null,
                    singleLine = true
                )
                if (error != null) Text(error!!, color = NotificationRed, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isBlank()) error = "Location cannot be empty" else onSave(text)
            }) { Text("Save", color = DarkNavy, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = DarkNavy) } },
        containerColor = ScreenWhite
    )
}
