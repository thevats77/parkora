package com.example.parkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkingSpot
import com.example.parkora.ui.theme.ChipGray
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.TextGray

/** "₹40/hr • ₹300/day" – used on cards and on the details screen. */
fun priceText(spot: ParkingSpot): String = "₹${spot.pricePerHour}/hr • ₹${spot.pricePerDay}/day"

/** "Rent", "Sale" or "Rent, Sale" */
fun availableForText(spot: ParkingSpot): String =
    listOfNotNull(if (spot.availableForRent) "Rent" else null, if (spot.availableForSale) "Sale" else null)
        .joinToString(", ")

/**
 * The card on the Home screen (Figure 1 in the synopsis):
 * photo, parking number, address, price, "For Rent" chip, "Book Now" button,
 * size, society name and suitable vehicles.
 */
@Composable
fun ParkingCard(spot: ParkingSpot, onClick: () -> Unit, onBookClick: () -> Unit) {
    CreamCard(onClick = onClick) {
        ParkingImage(
            path = spot.imagePaths.firstOrNull(),
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
        )
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(spot.parkingNumber, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                if (spot.isVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    VerifiedBadge(size = 18.dp)
                }
            }
            VSpace(8.dp)
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("${spot.address}, ${spot.city}, India", fontSize = 14.sp, color = DarkNavy)
            }
            VSpace(8.dp)
            SpaceBetweenRow {
                Column {
                    Text(priceText(spot), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                    VSpace(6.dp)
                    Text(
                        text = "For ${availableForText(spot)}",
                        fontSize = 11.sp,
                        color = TextGray,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ChipGray)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                if (spot.availableForRent) {
                    YellowButton(text = "Book Now", onClick = onBookClick, small = true)
                }
            }
            VSpace(12.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallInfo(Icons.Filled.Straighten, "${spot.lengthFeet}×${spot.widthFeet} ft")
                Spacer(modifier = Modifier.width(16.dp))
                SmallInfo(Icons.Filled.Apartment, spot.societyName)
            }
            VSpace(6.dp)
            SmallInfo(Icons.Filled.DirectionsCar, spot.vehicleTypes.joinToString(", "))
        }
    }
}

/** Tiny icon + text pair, e.g. 🚗 SUV, Sedan */
@Composable
fun SmallInfo(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, color = DarkNavy, maxLines = 1)
    }
}
