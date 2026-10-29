package com.example.parkora.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.parkora.data.BookingStatus
import com.example.parkora.data.ParkingSpot
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.data.User
import com.example.parkora.ui.components.BookingCard
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.OutlineButton
import com.example.parkora.ui.components.ParkingImage
import com.example.parkora.ui.components.TabSwitcher
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
 * ADMIN PORTAL (only visible to the admin account – FirebaseConfig.ADMIN_EMAIL)
 * ---------------------------------------------------------------------------
 *   Overview  – live numbers + "Add MUJ demo spots"
 *   Spots     – verify / un-verify listings, remove a listing
 *   Bookings  – every booking on Parkora, cancel an active one
 *   Users     – everyone who signed in, block / unblock
 * The Firestore security rules also check the admin email on the server, so a normal
 * user cannot use these functions even with a modified app.
 */
@Composable
fun AdminPortalScreen(onBack: () -> Unit, onSpotClick: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableIntStateOf(0) }
    var spotToRemove by remember { mutableStateOf<ParkingSpot?>(null) }

    /** Runs an admin action and shows the result as a Toast. */
    fun run(successMessage: String, action: suspend () -> String?) {
        scope.launch {
            val error = action()
            Toast.makeText(context, error ?: successMessage, Toast.LENGTH_SHORT).show()
        }
    }

    if (ParkoraRepository.currentUser?.isAdmin != true) {
        EmptyState("Only the Parkora admin can open this page.")
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(
            title = "Admin Portal",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
            bottomSpacing = 0.dp
        ) {
            TabSwitcher(tabs = listOf("Overview", "Spots", "Bookings", "Users"), selectedIndex = tab, onTabSelected = { tab = it })
        }

        val listPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
        Box(modifier = Modifier.weight(1f).navigationBarsPadding()) {
            when (tab) {
                0 -> OverviewTab(onAddDemoSpots = { run("4 MUJ demo spots are now live") { ParkoraRepository.adminAddMujDemoSpots() } })

                1 -> {
                    val spots = ParkoraRepository.parkingSpots.sortedWith(compareBy({ it.isVerified }, { -it.createdAt }))
                    if (spots.isEmpty()) EmptyState("No listings yet. Add the MUJ demo spots from Overview.")
                    LazyColumn(contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(spots, key = { it.id }) { spot ->
                            AdminSpotCard(
                                spot = spot,
                                onOpen = { onSpotClick(spot.id) },
                                onToggleVerified = {
                                    run(if (spot.isVerified) "Verification removed" else "Listing verified") {
                                        ParkoraRepository.adminSetSpotVerified(spot.id, !spot.isVerified)
                                    }
                                },
                                onRemove = { spotToRemove = spot }
                            )
                        }
                    }
                }

                2 -> {
                    val all = ParkoraRepository.bookings.sortedByDescending { it.createdAt }
                    if (all.isEmpty()) EmptyState("No bookings on Parkora yet.")
                    LazyColumn(contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(all, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                isVerified = ParkoraRepository.getSpot(booking.spotId)?.isVerified == true,
                                onClick = { onSpotClick(booking.spotId) }
                            ) {
                                if (ParkoraRepository.isOngoing(booking)) {
                                    OutlineButton(text = "Cancel booking", color = RejectedRed, onClick = {
                                        run("Booking cancelled") { ParkoraRepository.adminCancelBooking(booking.id) }
                                    })
                                }
                            }
                        }
                    }
                }

                else -> {
                    val users = ParkoraRepository.allUsers
                    LazyColumn(contentPadding = listPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(users, key = { it.id }) { user ->
                            AdminUserRow(user = user, onToggleBlocked = {
                                run(if (user.isBlocked) "${user.name} unblocked" else "${user.name} blocked") {
                                    ParkoraRepository.adminSetUserBlocked(user.id, !user.isBlocked)
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    spotToRemove?.let { spot ->
        AlertDialog(
            onDismissRequest = { spotToRemove = null },
            title = { Text("Remove ${spot.parkingNumber}?") },
            text = { Text("The listing is deleted and its upcoming bookings are cancelled. The renters are notified.") },
            confirmButton = {
                TextButton(onClick = {
                    spotToRemove = null
                    run("Listing removed") { ParkoraRepository.adminDeleteSpot(spot.id) }
                }) { Text("Remove", color = RejectedRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { spotToRemove = null }) { Text("Cancel", color = DarkNavy) } },
            containerColor = ScreenWhite
        )
    }
}

@Composable
private fun OverviewTab(onAddDemoSpots: () -> Unit) {
    val spots = ParkoraRepository.parkingSpots
    val bookings = ParkoraRepository.bookings
    val bookingValue = bookings.filter { it.status == BookingStatus.APPROVED || it.status == BookingStatus.COMPLETED }
        .sumOf { it.totalPrice }

    val stats = listOf(
        "Users" to "${ParkoraRepository.allUsers.size}",
        "Parking spots" to "${spots.size}",
        "Awaiting verification" to "${spots.count { !it.isVerified }}",
        "Bookings" to "${bookings.size}",
        "Active bookings" to "${bookings.count { ParkoraRepository.isOngoing(it) }}",
        "Booking value" to "₹$bookingValue"
    )

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(stats.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value) ->
                    CreamCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                            Text(label, fontSize = 13.sp, color = TextGray)
                        }
                    }
                }
            }
        }
        item {
            VSpace(8.dp)
            CreamCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Demo listings near MUJ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                    Text(
                        "Adds 4 sample parking spots around Manipal University Jaipur (Dehmi Kalan, Ajmer Road, Bagru). " +
                            "Pressing it again only refreshes them – no duplicates.",
                        fontSize = 13.sp, color = TextGray
                    )
                    VSpace(12.dp)
                    YellowButton(text = "Add MUJ demo spots", onClick = onAddDemoSpots, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun AdminSpotCard(spot: ParkingSpot, onOpen: () -> Unit, onToggleVerified: () -> Unit, onRemove: () -> Unit) {
    CreamCard(onClick = onOpen) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                ParkingImage(spot.imagePaths.firstOrNull(), Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(spot.parkingNumber, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = DarkNavy)
                        if (spot.isVerified) { Spacer(Modifier.width(5.dp)); VerifiedBadge(16.dp) }
                    }
                    Text("${spot.societyName}, ${spot.city}", fontSize = 13.sp, color = DarkNavy, maxLines = 1)
                    Text("Owner: ${spot.ownerName} • ${priceText(spot)}", fontSize = 12.sp, color = TextGray, maxLines = 1)
                    if (!spot.isVerified) Text("Awaiting verification", fontSize = 12.sp, color = RejectedRed)
                }
            }
            VSpace(10.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                OutlineButton(text = "Remove", color = RejectedRed, onClick = onRemove)
                if (spot.isVerified) OutlineButton(text = "Un-verify", onClick = onToggleVerified)
                else YellowButton(text = "Verify", small = true, onClick = onToggleVerified)
            }
        }
    }
}

@Composable
private fun AdminUserRow(user: User, onToggleBlocked: () -> Unit) {
    CreamCard(cornerRadius = 10.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(ParkoraYellow),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImagePath != null) ParkingImage(user.profileImagePath, Modifier.fillMaxSize())
                else Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = DarkNavy)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy, maxLines = 1)
                Text(user.email, fontSize = 12.sp, color = TextGray, maxLines = 1)
                Text(
                    when {
                        user.isAdmin -> "Admin"
                        user.isBlocked -> "Blocked"
                        else -> "User"
                    },
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = if (user.isBlocked) RejectedRed else DarkNavy
                )
            }
            if (!user.isAdmin) {
                if (user.isBlocked) YellowButton(text = "Unblock", small = true, showArrow = false, onClick = onToggleBlocked)
                else OutlineButton(text = "Block", color = RejectedRed, onClick = onToggleBlocked)
            }
        }
    }
}
