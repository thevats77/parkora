package com.example.parkora.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.BookingStatus
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.BottomTab
import com.example.parkora.ui.components.ParkingImage
import com.example.parkora.ui.components.ParkoraBottomBar
import com.example.parkora.ui.components.ProfileMenuItem
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.utils.ImageUtils
import kotlinx.coroutines.launch

/*
 * PROFILE SCREEN  (Figure 2 – "Profile & Account Management")
 * -----------------------------------------------------------
 * Yellow avatar with a camera button, "Name | email | phone",
 * and the menu: My Parking Spots, My Bookings, Booking Requests,
 * Edit Profile, Contact Us, Privacy Policy, Logout – plus "Admin Portal" for the admin account.
 */
@Composable
fun ProfileScreen(
    onMySpotsClick: () -> Unit,
    onMyBookingsClick: () -> Unit,
    onBookingRequestsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAdminPortalClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val user = ParkoraRepository.currentUser ?: return
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val mySpotCount = ParkoraRepository.mySpots().size
    val pendingRequestCount = ParkoraRepository.bookingRequestsForMe().count { it.status == BookingStatus.PENDING }

    // Opens the phone's photo picker; the chosen photo is shrunk and saved online as the profile picture
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val photo = ImageUtils.uriToDataUrl(context, uri, maxSize = 400, quality = 80)
            if (photo == null) {
                Toast.makeText(context, "Could not read that photo", Toast.LENGTH_SHORT).show()
            } else {
                scope.launch {
                    val error = ParkoraRepository.updateProfilePhoto(photo)
                    Toast.makeText(context, error ?: "Profile photo updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            YellowHeader(title = "Profile")

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VSpace(20.dp)

                // ----- Avatar with camera button -----
                Box {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(ParkoraYellow)
                            .border(1.dp, DarkNavy.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profileImagePath != null) {
                            ParkingImage(user.profileImagePath, Modifier.fillMaxSize())
                        } else {
                            Text(initialsOf(user.name), fontSize = 38.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .clickable {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = "Change photo", tint = DarkNavy, modifier = Modifier.size(20.dp))
                    }
                }

                VSpace(14.dp)
                Text(
                    text = "${user.name} |\n${user.email} |\n${user.phone.ifBlank { "Add your phone in Edit Profile" }}",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = DarkNavy,
                    textAlign = TextAlign.Center
                )
                VSpace(18.dp)

                // ----- Menu -----
                Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                    if (user.isAdmin) {
                        ProfileMenuItem(Icons.Filled.AdminPanelSettings, "Admin Portal", onAdminPortalClick, trailingText = "Admin")
                    }
                    ProfileMenuItem(Icons.Filled.DirectionsCar, "My Parking Spots", onMySpotsClick,
                        trailingText = if (mySpotCount > 0) "$mySpotCount" else null)
                    ProfileMenuItem(Icons.Filled.CalendarMonth, "My Bookings", onMyBookingsClick)
                    ProfileMenuItem(Icons.Filled.PendingActions, "Booking Requests", onBookingRequestsClick,
                        trailingText = if (pendingRequestCount > 0) "$pendingRequestCount new" else null)
                    ProfileMenuItem(Icons.Filled.ManageAccounts, "Edit Profile", onEditProfileClick)
                    ProfileMenuItem(Icons.Filled.SupportAgent, "Contact Us", onContactUsClick)
                    ProfileMenuItem(Icons.Filled.Policy, "Privacy Policy", onPrivacyPolicyClick)
                    ProfileMenuItem(Icons.AutoMirrored.Filled.Logout, "Logout", { showLogoutDialog = true })
                }
                VSpace(140.dp) // room for the bottom bar
            }
        }

        ParkoraBottomBar(
            selectedTab = BottomTab.PROFILE,
            onHomeClick = onHomeClick,
            onAddClick = onAddClick,
            onProfileClick = {},
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    // The navigation graph shows the Login screen as soon as currentUser becomes null
                    scope.launch { ParkoraRepository.logout() }
                }) { Text("Logout", color = DarkNavy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = DarkNavy) } },
            containerColor = ScreenWhite
        )
    }
}

/** "Vatsal Sharma" -> "VS" */
private fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
