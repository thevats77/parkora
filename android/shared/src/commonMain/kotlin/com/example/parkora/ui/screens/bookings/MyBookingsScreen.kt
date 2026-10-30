package com.example.parkora.ui.screens.bookings

import com.example.parkora.ui.components.showToast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.parkora.data.Booking
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.BookingCard
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.TabSwitcher
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.utils.DateTimeUtils
import kotlinx.coroutines.launch

/*
 * MY BOOKINGS  (Figure 4 in the synopsis)
 * ---------------------------------------
 * Bookings made by the logged-in user, split into two tabs:
 *   Ongoing = Pending or Approved
 *   Past    = Completed, Cancelled, Rejected or Expired
 * A booking can be cancelled before its check-in time.
 */
@Composable
fun MyBookingsScreen(onBack: () -> Unit, onSpotClick: (String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    val scope = rememberCoroutineScope()

    val allBookings = ParkoraRepository.myBookings()
    val ongoing = allBookings.filter { ParkoraRepository.isOngoing(it) }
    val past = allBookings.filter { !ParkoraRepository.isOngoing(it) }.reversed() // latest first
    val shownBookings = if (selectedTab == 0) ongoing else past

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(
            title = "My Bookings",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
            bottomSpacing = 0.dp
        ) {
            TabSwitcher(tabs = listOf("Ongoing", "Past"), selectedIndex = selectedTab, onTabSelected = { selectedTab = it })
        }

        if (shownBookings.isEmpty()) {
            EmptyState(if (selectedTab == 0) "You have no ongoing bookings.\nFind a spot on the Home screen!" else "No past bookings yet.")
        } else {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(shownBookings, key = { it.id }) { booking ->
                    val spot = ParkoraRepository.getSpot(booking.spotId)
                    BookingCard(
                        booking = booking,
                        isVerified = spot?.isVerified == true,
                        onClick = { if (spot != null) onSpotClick(spot.id) }
                    ) {
                        if (ParkoraRepository.canCancel(booking)) {
                            YellowButton(text = "Cancel Booking", onClick = { bookingToCancel = booking }, small = true)
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog before cancelling
    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("Cancel booking?") },
            text = { Text("Cancel your booking for ${booking.parkingNumber} on ${DateTimeUtils.formatForBookingCard(booking.checkInMillis)}?") },
            confirmButton = {
                TextButton(onClick = {
                    bookingToCancel = null
                    scope.launch {
                        val error = ParkoraRepository.cancelBooking(booking.id)
                        showToast(error ?: "Booking cancelled")
                    }
                }) { Text("Yes, cancel", color = DarkNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { bookingToCancel = null }) { Text("No", color = DarkNavy) } },
            containerColor = ScreenWhite
        )
    }
}
