package com.example.parkora.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.NotificationRed
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.TextGray
import com.example.parkora.utils.DateTimeUtils

/*
 * NOTIFICATIONS – opened with the bell on the Home screen.
 * These are IN-APP messages saved by the repository when a booking is
 * requested, approved, rejected or cancelled. They are not push
 * notifications (that would need Firebase Cloud Messaging – future scope).
 */
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notifications = ParkoraRepository.myNotifications()

    // When the user leaves this screen, mark everything as read (removes the red dot)
    DisposableEffect(Unit) {
        onDispose { ParkoraRepository.markMyNotificationsRead() }
    }

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(title = "Notifications", navigationIcon = Icons.AutoMirrored.Filled.ArrowBack, onNavigationClick = onBack)

        if (notifications.isEmpty()) {
            EmptyState("No notifications yet.")
        } else {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    CreamCard(cornerRadius = 10.dp) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            if (!notification.isRead) {
                                Box(modifier = Modifier.padding(top = 6.dp).size(9.dp).background(NotificationRed, CircleShape))
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column {
                                Text(notification.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                                Text(notification.message, fontSize = 14.sp, color = DarkNavy)
                                Text(DateTimeUtils.formatForNotification(notification.createdAt), fontSize = 12.sp, color = TextGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
