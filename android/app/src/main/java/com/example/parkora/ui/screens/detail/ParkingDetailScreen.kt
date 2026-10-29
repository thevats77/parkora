package com.example.parkora.ui.screens.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkingSpot
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.ErrorText
import com.example.parkora.ui.components.ParkingImage
import com.example.parkora.ui.components.PickerField
import com.example.parkora.ui.components.SpaceBetweenRow
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.components.VerifiedBadge
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.availableForText
import com.example.parkora.ui.components.priceText
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.StarYellow
import com.example.parkora.ui.theme.TextGray
import com.example.parkora.utils.BookingCalculator
import com.example.parkora.utils.DateTimeUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.launch

/*
 * PARKING DETAILS & BOOKING  (Figure 5 in the synopsis)
 * -----------------------------------------------------
 * Shows the photo, parking number, location, vehicles and price.
 * The user picks check-in / check-out date and time; the Summary box
 * recalculates the duration and price every time a value changes
 * (BookingCalculator.calculate). "Book Now" creates a PENDING booking.
 */
@Composable
fun ParkingDetailScreen(spotId: String, onBack: () -> Unit, onViewBookings: () -> Unit) {
    val spot = ParkoraRepository.getSpot(spotId)
    if (spot == null) {
        EmptyState("This parking spot no longer exists.")
        return
    }

    val context = LocalContext.current

    // ----- Booking form state (default: next full hour, for 2 hours) -----
    val defaultStart = remember { DateTimeUtils.nextFullHour() }
    var checkInDate by remember { mutableStateOf(defaultStart.toLocalDate()) }
    var checkInTime by remember { mutableStateOf(defaultStart.toLocalTime()) }
    var checkOutDate by remember { mutableStateOf(defaultStart.plusHours(2).toLocalDate()) }
    var checkOutTime by remember { mutableStateOf(defaultStart.plusHours(2).toLocalTime()) }
    var summaryExpanded by remember { mutableStateOf(true) }
    var bookingError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var isBooking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Recalculated on every recomposition, so it is always up to date
    val checkInMillis = DateTimeUtils.toMillis(checkInDate, checkInTime)
    val checkOutMillis = DateTimeUtils.toMillis(checkOutDate, checkOutTime)
    val summary = BookingCalculator.calculate(checkInMillis, checkOutMillis, spot.pricePerHour, spot.pricePerDay)

    val isOwnSpot = spot.ownerId == ParkoraRepository.currentUser?.id
    val canBook = !isOwnSpot && spot.isAvailable && spot.availableForRent

    // ----- Helpers that open Android's date and time pickers -----
    fun pickDate(current: LocalDate, onPicked: (LocalDate) -> Unit) {
        val dialog = DatePickerDialog(
            context,
            { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) }, // month is 0-based
            current.year, current.monthValue - 1, current.dayOfMonth
        )
        // Past dates cannot be selected
        dialog.datePicker.minDate = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        dialog.show()
    }

    fun pickTime(current: LocalTime, onPicked: (LocalTime) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onPicked(LocalTime.of(hour, minute)) },
            current.hour, current.minute, false // false = 12-hour clock with AM/PM
        ).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ----- Photo with back button -----
            Box {
                ParkingImage(
                    path = spot.imagePaths.getOrNull(selectedImageIndex),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                )
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(46.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkNavy)
                }
            }

            // Thumbnails when the owner uploaded more than one photo
            if (spot.imagePaths.size > 1) {
                Row(modifier = Modifier.padding(start = 20.dp, top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    spot.imagePaths.forEachIndexed { index, path ->
                        ParkingImage(
                            path = path,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, if (index == selectedImageIndex) ParkoraYellow else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedImageIndex = index }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                SpotInfoSection(spot)

                // ----- Check-in / Check-out pickers -----
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PickerField("Check-In Date", DateTimeUtils.formatDate(checkInDate), Icons.AutoMirrored.Filled.Login,
                        onClick = { pickDate(checkInDate) { checkInDate = it; bookingError = null } }, modifier = Modifier.weight(1f))
                    PickerField("Check-In Time", DateTimeUtils.formatTime(checkInTime), Icons.Filled.Schedule,
                        onClick = { pickTime(checkInTime) { checkInTime = it; bookingError = null } }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PickerField("Check-Out Date", DateTimeUtils.formatDate(checkOutDate), Icons.AutoMirrored.Filled.Logout,
                        onClick = { pickDate(checkOutDate) { checkOutDate = it; bookingError = null } }, modifier = Modifier.weight(1f))
                    PickerField("Check-Out Time", DateTimeUtils.formatTime(checkOutTime), Icons.Filled.Schedule,
                        onClick = { pickTime(checkOutTime) { checkOutTime = it; bookingError = null } }, modifier = Modifier.weight(1f))
                }

                // ----- Summary box -----
                VSpace(20.dp)
                CreamCard {
                    Column(modifier = Modifier.padding(18.dp)) {
                        SpaceBetweenRow(modifier = Modifier.clickable { summaryExpanded = !summaryExpanded }) {
                            Text("Summary", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                            Icon(if (summaryExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = DarkNavy)
                        }
                        if (summaryExpanded) {
                            VSpace(12.dp)
                            if (summary == null) {
                                Text("Check-out must be after check-in.", color = Color(0xFFDC2626), fontSize = 14.sp)
                            } else {
                                SummaryRow("Duration", BookingCalculator.durationText(summary.totalHours))
                                SummaryRow("Item Total", "${summary.billedUnits} ${summary.unitName}")
                                SummaryRow(
                                    "Base Price (${summary.billedUnits} × ₹${summary.unitPrice}.00/${summary.shortUnit})",
                                    "₹${summary.totalPrice}.00"
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEFE6C0))
                                SummaryRow("Total Amount", "₹${summary.totalPrice}.00", bold = true)
                                Text(
                                    "Payment is made directly to the owner (no online payment in this version).",
                                    fontSize = 11.sp, color = TextGray, modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
                VSpace(12.dp)
            }
        }

        // ----- Fixed "Book Now" button at the bottom -----
        Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
            when {
                isOwnSpot -> Text("This is your own parking spot.", color = TextGray, fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                !spot.availableForRent -> Text("This spot is listed for sale only. Contact the owner: ${spot.ownerName}.",
                    color = TextGray, fontSize = 14.sp)
                !spot.isAvailable -> Text("The owner has paused bookings for this spot.", color = TextGray, fontSize = 14.sp)
            }
            ErrorText(bookingError)
            VSpace(6.dp)
            YellowButton(
                text = if (isBooking) "Booking…" else "Book Now",
                enabled = canBook && !isBooking,
                onClick = {
                    // createBooking returns null on success, or an error message shown above the button
                    isBooking = true
                    scope.launch {
                        bookingError = ParkoraRepository.createBooking(spot, checkInMillis, checkOutMillis)
                        isBooking = false
                        if (bookingError == null) showSuccessDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onBack() },
            title = { Text("Booking requested!") },
            text = {
                Text("Your request for ${spot.parkingNumber} was sent to ${spot.ownerName}. " +
                    "It will show as \"Pending\" in My Bookings until the owner approves it.")
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false; onViewBookings() }) {
                    Text("View My Bookings", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false; onBack() }) { Text("OK", color = DarkNavy) }
            },
            containerColor = ScreenWhite
        )
    }
}

/** Parking number, verified tick, "Available For" badge, address, stars, vehicles and price. */
@Composable
private fun SpotInfoSection(spot: ParkingSpot) {
    SpaceBetweenRow {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(spot.parkingNumber, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            if (spot.isVerified) {
                Spacer(modifier = Modifier.width(8.dp))
                VerifiedBadge(size = 26.dp)
            }
        }
        Text(
            text = buildAnnotatedString {
                append("Available For ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(availableForText(spot)) }
            },
            fontSize = 12.sp,
            color = DarkNavy,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ParkoraYellow)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
    if (!spot.isVerified) {
        Text("Verification pending", fontSize = 12.sp, color = TextGray)
    }
    VSpace(10.dp)
    SpaceBetweenRow {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.PersonPinCircle, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("${spot.address}, ${spot.city}, India", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkNavy)
        }
        RatingStars(spot.rating)
    }
    VSpace(10.dp)
    SpaceBetweenRow {
        Text(
            "Suitable for ${spot.vehicleTypes.joinToString(", ")}",
            fontSize = 13.sp, color = TextGray, modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(priceText(spot), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = DarkNavy)
    }
    VSpace(6.dp)
    Text(
        "${spot.parkingType} • ${spot.lengthFeet} × ${spot.widthFeet} ft • ${spot.societyName} • Owner: ${spot.ownerName}",
        fontSize = 12.sp, color = TextGray
    )
    VSpace(4.dp)
}

@Composable
private fun RatingStars(rating: Int) {
    if (rating == 0) {
        Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        return
    }
    Row {
        for (star in 1..5) {
            Icon(
                if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = StarYellow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    SpaceBetweenRow(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 14.sp, color = DarkNavy, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = DarkNavy, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
}
