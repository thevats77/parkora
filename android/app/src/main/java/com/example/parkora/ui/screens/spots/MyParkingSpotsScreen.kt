package com.example.parkora.ui.screens.spots

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkingSpot
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.ParkingImage
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.components.VerifiedBadge
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.components.priceText
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.RejectedRed
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.TextGray
import kotlinx.coroutines.launch

/*
 * MY PARKING SPOTS  (Owner Dashboard)
 * -----------------------------------
 * Spots listed by the logged-in user. The owner can:
 *   - switch a spot ON/OFF for new bookings,
 *   - delete a spot (only if it has no active bookings),
 *   - list a new spot.
 */
@Composable
fun MyParkingSpotsScreen(onBack: () -> Unit, onAddClick: () -> Unit, onSpotClick: (String) -> Unit) {
    val context = LocalContext.current
    val mySpots = ParkoraRepository.mySpots()
    var spotToDelete by remember { mutableStateOf<ParkingSpot?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(title = "My Parking Spots", navigationIcon = Icons.AutoMirrored.Filled.ArrowBack, onNavigationClick = onBack)

        if (mySpots.isEmpty()) {
            Column(modifier = Modifier.weight(1f)) {
                EmptyState("You have not listed any parking spot yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mySpots, key = { it.id }) { spot ->
                    MySpotCard(
                        spot = spot,
                        onClick = { onSpotClick(spot.id) },
                        onAvailabilityChange = { available ->
                            scope.launch {
                                ParkoraRepository.setSpotAvailability(spot.id, available)
                                    ?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            }
                        },
                        onDeleteClick = { spotToDelete = spot }
                    )
                }
            }
        }

        YellowButton(
            text = "List a New Parking Spot",
            onClick = onAddClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }

    spotToDelete?.let { spot ->
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            title = { Text("Delete ${spot.parkingNumber}?") },
            text = { Text("This removes the listing. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    spotToDelete = null
                    scope.launch {
                        val error = ParkoraRepository.deleteSpot(spot.id)
                        Toast.makeText(context, error ?: "Parking spot deleted", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Delete", color = RejectedRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { spotToDelete = null }) { Text("Cancel", color = DarkNavy) } },
            containerColor = ScreenWhite
        )
    }
}

@Composable
private fun MySpotCard(
    spot: ParkingSpot,
    onClick: () -> Unit,
    onAvailabilityChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    CreamCard(onClick = onClick) {
        Row(modifier = Modifier.padding(14.dp)) {
            ParkingImage(spot.imagePaths.firstOrNull(), Modifier.size(84.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(spot.parkingNumber, fontSize = 19.sp, fontWeight = FontWeight.Medium, color = DarkNavy)
                    Spacer(modifier = Modifier.width(5.dp))
                    if (spot.isVerified) VerifiedBadge(16.dp)
                }
                Text(spot.societyName, fontSize = 13.sp, color = DarkNavy)
                Text(priceText(spot), fontSize = 13.sp, color = DarkNavy, fontWeight = FontWeight.Medium)
                if (!spot.isVerified) Text("Verification pending", fontSize = 12.sp, color = TextGray)
                VSpace(4.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (spot.isAvailable) "Accepting bookings" else "Bookings paused", fontSize = 12.sp, color = TextGray,
                        modifier = Modifier.weight(1f))
                    Switch(
                        checked = spot.isAvailable,
                        onCheckedChange = onAvailabilityChange,
                        colors = SwitchDefaults.colors(checkedTrackColor = ParkoraYellow, checkedThumbColor = DarkNavy)
                    )
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete spot", tint = RejectedRed)
                    }
                }
            }
        }
    }
}
