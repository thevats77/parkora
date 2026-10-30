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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkora.data.BookingStatus
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.BookingCard
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.OutlineButton
import com.example.parkora.ui.components.TabSwitcher
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.RejectedRed
import com.example.parkora.ui.theme.ScreenWhite
import kotlinx.coroutines.launch

/*
 * BOOKING REQUESTS  (Owner Dashboard)
 * -----------------------------------
 * Bookings that OTHER users made for MY parking spots.
 *   "Pending" tab – the owner can Approve or Reject.
 *   "History" tab – requests already answered (or cancelled/expired).
 */
@Composable
fun BookingRequestsScreen(onBack: () -> Unit, onSpotClick: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    val requests = ParkoraRepository.bookingRequestsForMe()
    val pending = requests.filter { it.status == BookingStatus.PENDING }
    val history = requests.filter { it.status != BookingStatus.PENDING }
    val shown = if (selectedTab == 0) pending else history

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(
            title = "Booking Requests",
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
            bottomSpacing = 0.dp
        ) {
            TabSwitcher(tabs = listOf("Pending (${pending.size})", "History"), selectedIndex = selectedTab, onTabSelected = { selectedTab = it })
        }

        if (shown.isEmpty()) {
            EmptyState(
                if (selectedTab == 0) "No new booking requests.\nRequests for your parking spots will appear here."
                else "No answered requests yet."
            )
        } else {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
                contentPadding = PaddingValues(22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                items(shown, key = { it.id }) { booking ->
                    val spot = ParkoraRepository.getSpot(booking.spotId)
                    BookingCard(
                        booking = booking,
                        isVerified = spot?.isVerified == true,
                        onClick = { if (spot != null) onSpotClick(spot.id) }
                    ) {
                        if (booking.status == BookingStatus.PENDING) {
                            OutlineButton(text = "Reject", color = RejectedRed, onClick = {
                                scope.launch {
                                    val error = ParkoraRepository.respondToBooking(booking.id, approve = false)
                                    showToast(error ?: "Request rejected")
                                }
                            })
                            YellowButton(text = "Approve", small = true, onClick = {
                                scope.launch {
                                    val error = ParkoraRepository.respondToBooking(booking.id, approve = true)
                                    showToast(error ?: "Booking approved")
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
