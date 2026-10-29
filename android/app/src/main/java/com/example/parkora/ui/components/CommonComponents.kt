package com.example.parkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.parkora.R
import com.example.parkora.data.BookingStatus
import com.example.parkora.ui.theme.ApprovedGreen
import com.example.parkora.ui.theme.ButtonYellowBottom
import com.example.parkora.ui.theme.ButtonYellowTop
import com.example.parkora.ui.theme.CardBorder
import com.example.parkora.ui.theme.CardCream
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.LightGray
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.ParkoraYellowLight
import com.example.parkora.ui.theme.PendingOrange
import com.example.parkora.ui.theme.RejectedRed
import com.example.parkora.ui.theme.TextGray
import com.example.parkora.ui.theme.VerifiedGreen
import com.example.parkora.utils.ImageUtils

/*
 * REUSABLE UI PIECES
 * ------------------
 * Small @Composable functions that are used on many screens.
 * Writing them once here keeps every screen looking the same
 * (same yellow header, same buttons, same text boxes) – just like the screenshots.
 */

/**
 * The yellow header with rounded bottom corners seen at the top of
 * Profile, List Parking Spot, My Bookings, etc.
 * [extraContent] lets a screen add something under the title (e.g. the Ongoing/Past tabs).
 */
@Composable
fun YellowHeader(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    bottomSpacing: Dp = 12.dp,
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(ParkoraYellow)
            .drawBehind {
                // Two soft, lighter circles – the decoration visible in the screenshots
                drawCircle(ParkoraYellowLight, radius = size.width * 0.42f, center = Offset(size.width * 0.12f, -size.height * 0.15f))
                drawCircle(ParkoraYellowLight.copy(alpha = 0.5f), radius = size.width * 0.35f, center = Offset(size.width * 0.75f, -size.height * 0.45f))
            }
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 8.dp)
            ) {
                if (navigationIcon != null) {
                    IconButton(onClick = onNavigationClick, modifier = Modifier.align(Alignment.CenterStart)) {
                        // Screen readers say "Close" for the ✕ icon and "Back" for the arrow
                        val description = if (navigationIcon == Icons.Filled.Close) "Close" else "Back"
                        Icon(navigationIcon, contentDescription = description, tint = DarkNavy, modifier = Modifier.size(28.dp))
                    }
                }
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkNavy,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            extraContent()
            Spacer(modifier = Modifier.height(bottomSpacing))
        }
    }
}

/**
 * The big yellow gradient button ("Next ➜", "Book Now ➜").
 * Use small = true for the little "Book Now" / "Cancel Booking" buttons on cards.
 */
@Composable
fun YellowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    showArrow: Boolean = true,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(if (small) 8.dp else 12.dp)
    val alpha = if (enabled) 1f else 0.5f
    Box(
        modifier = modifier
            .height(if (small) 34.dp else 54.dp)
            .shadow(if (small) 3.dp else 2.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(ButtonYellowTop.copy(alpha = alpha), ButtonYellowBottom.copy(alpha = alpha))))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (small) 12.dp else 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                fontSize = if (small) 12.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            if (showArrow) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Filled.ArrowCircleRight,
                    contentDescription = null,
                    tint = DarkNavy,
                    modifier = Modifier.size(if (small) 14.dp else 18.dp)
                )
            }
        }
    }
}

/** A white button with a dark border, used for secondary actions like "Reject". */
@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = DarkNavy) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .border(1.dp, color, shape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

/**
 * The cream box with an icon, a thin divider and some content.
 * Used by both text fields and the date/time picker fields.
 */
@Composable
fun FieldBox(
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    var boxModifier = modifier
        .fillMaxWidth()
        .height(54.dp)
        .clip(shape)
        .background(CardCream)
        .border(1.dp, if (isError) RejectedRed else Color(0xFFF7EDB0), shape)
    if (onClick != null) boxModifier = boxModifier.clickable(onClick = onClick)

    Row(modifier = boxModifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(26.dp).background(Color(0xFFE6E1C8)))
            Spacer(modifier = Modifier.width(14.dp))
        }
        Box(modifier = Modifier.weight(1f)) { content() }
        trailing()
    }
}

/** Small grey label written above a field, e.g. "Parking Number". */
@Composable
fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextGray,
        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp, top = 12.dp)
    )
}

/** Red message shown under a field when validation fails. */
@Composable
fun ErrorText(error: String?) {
    if (error != null) {
        Text(
            text = error,
            color = RejectedRed,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 6.dp, top = 4.dp)
        )
    }
}

/** A labelled text box in the Parkora style, with an optional error message. */
@Composable
fun ParkoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        FieldLabel(label)
        FieldBox(
            icon = icon,
            isError = error != null,
            trailing = {
                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Show or hide password",
                            tint = TextGray
                        )
                    }
                }
            }
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = DarkNavy),
                cursorBrush = SolidColor(DarkNavy),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = TextGray.copy(alpha = 0.8f))
                    }
                    innerTextField()
                }
            )
        }
        ErrorText(error)
    }
}

/** A field that opens a picker when tapped (used for check-in/check-out date and time). */
@Composable
fun PickerField(label: String, value: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FieldLabel(label)
        FieldBox(icon = icon, onClick = onClick) {
            Text(value, style = MaterialTheme.typography.bodyLarge, color = DarkNavy)
        }
    }
}

/**
 * Shows a parking (or profile) photo. The path can be:
 *  - "demo_parking_1" … "demo_parking_4": pictures built into the app (res/drawable-nodpi)
 *  - "data:image/jpeg;base64,…": a photo uploaded by a user, stored in Firestore (see ImageUtils)
 *  - "https://…": an internet picture, e.g. the Google profile photo
 * AsyncImage comes from the Coil library and loads the image in the background.
 */
@Composable
fun ParkingImage(path: String?, modifier: Modifier = Modifier) {
    val imageModel: Any = remember(path) {
        when {
            path.isNullOrEmpty() || path == "demo_parking_1" -> R.drawable.demo_parking_1
            path == "demo_parking_2" -> R.drawable.demo_parking_2
            path == "demo_parking_3" -> R.drawable.demo_parking_3
            path == "demo_parking_4" -> R.drawable.demo_parking_4
            // real photos used for the MUJ demo listings (credits: docs/PHOTO_CREDITS.md)
            path == "demo_muj_dk12" -> R.drawable.demo_muj_dk12
            path == "demo_muj_ae04" -> R.drawable.demo_muj_ae04
            path == "demo_muj_ae04_road" -> R.drawable.demo_muj_ae04_road
            path == "demo_muj_bg21" -> R.drawable.demo_muj_bg21
            path == "demo_muj_tw07" -> R.drawable.demo_muj_tw07
            ImageUtils.isDataUrl(path) -> ImageUtils.dataUrlToBytes(path)
            else -> path
        }
    }
    AsyncImage(
        model = imageModel,
        contentDescription = "Parking photo",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

/** Green tick shown next to verified parking numbers. */
@Composable
fun VerifiedBadge(size: Dp = 20.dp) {
    Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = VerifiedGreen, modifier = Modifier.size(size))
}

/** Coloured status label such as "Pending" (orange) or "Approved" (green). */
@Composable
fun StatusBadge(status: BookingStatus) {
    val (color, icon) = when (status) {
        BookingStatus.PENDING -> PendingOrange to Icons.Filled.Schedule
        BookingStatus.APPROVED, BookingStatus.COMPLETED -> ApprovedGreen to Icons.Filled.CheckCircle
        BookingStatus.REJECTED, BookingStatus.CANCELLED, BookingStatus.EXPIRED -> RejectedRed to Icons.Filled.Cancel
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(status.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** The cream rounded card with a thin border used for most list items. */
@Composable
fun CreamCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    cornerRadius: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    var cardModifier = modifier
        .fillMaxWidth()
        .shadow(3.dp, shape, ambientColor = ParkoraYellow, spotColor = ParkoraYellow)
        .clip(shape)
        .background(CardCream)
        .border(1.dp, borderColor, shape)
    if (onClick != null) cardModifier = cardModifier.clickable(onClick = onClick)
    Column(modifier = cardModifier, content = content)
}

/** Message shown when a list is empty. */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, color = TextGray, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}

/** A row in the Profile menu: icon | divider | title ........ → */
@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit, trailingText: String? = null) {
    CreamCard(cornerRadius = 8.dp, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Box(modifier = Modifier.width(1.5.dp).height(30.dp).background(DarkNavy))
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = DarkNavy, modifier = Modifier.weight(1f))
            if (trailingText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ParkoraYellow)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(trailingText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DarkNavy)
        }
    }
}

/** A selectable chip, e.g. vehicle types "SUV", "Sedan" in the listing form. */
@Composable
fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) ParkoraYellow else CardCream)
            .border(1.dp, if (selected) ButtonYellowBottom else Color(0xFFE8E2C0), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = DarkNavy)
    }
}

/** Padding used by most scrolling lists so content does not hide behind the bottom bar. */
val ListPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)

/** Grey divider line colour used in the step indicator. */
val StepInactiveColor = LightGray

/** Vertical space helper to keep screens tidy. */
@Composable
fun VSpace(height: Dp) = Spacer(modifier = Modifier.height(height))

/** Row with items spaced at both ends – used often in cards. */
@Composable
fun SpaceBetweenRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
