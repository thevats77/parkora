package com.example.parkora.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.Booking
import com.example.parkora.ui.theme.BookingCardBorder
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.TextGray
import com.example.parkora.utils.DateTimeUtils

/**
 * One booking (Figure 4 in the synopsis): photo, parking number, status,
 * "Booking By", date/time and buttons.
 * The [actions] slot lets each screen choose its own buttons:
 *   - My Bookings      -> "Cancel Booking"
 *   - Booking Requests -> "Reject" and "Approve"
 */
@Composable
fun BookingCard(
    booking: Booking,
    isVerified: Boolean,
    onClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CreamCard(borderColor = BookingCardBorder, onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                ParkingImage(
                    path = booking.spotImage,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SpaceBetweenRow {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(booking.parkingNumber, fontSize = 19.sp, fontWeight = FontWeight.Medium, color = DarkNavy)
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(5.dp))
                                VerifiedBadge(size = 16.dp)
                            }
                        }
                        StatusBadge(booking.status)
                    }
                    VSpace(4.dp)
                    Text("Booking By: ${booking.userName}", fontSize = 13.sp, color = DarkNavy)
                    Text(DateTimeUtils.formatForBookingCard(booking.checkInMillis), fontSize = 13.sp, color = DarkNavy)
                    Text(
                        "Till ${DateTimeUtils.formatForBookingCard(booking.checkOutMillis)}  •  ₹${booking.totalPrice}",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                content = actions
            )
        }
    }
}
