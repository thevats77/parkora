package com.example.parkora.ui.screens.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.TextGray

/*
 * CONTACT US and PRIVACY POLICY – two simple information screens.
 */

// TODO: replace with your real project/support email before submitting.
private const val SUPPORT_EMAIL = "support@parkora.example"

@Composable
fun ContactUsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    InfoScreenLayout(title = "Contact Us", onBack = onBack) {
        InfoCard("Need help?", "Questions about a booking, a listing or your account? Write to us and the Parkora team will reply.")
        InfoCard("Email", SUPPORT_EMAIL)
        InfoCard("About this project", "Parkora – Smart Peer-to-Peer Parking & Booking Application.\nAndroid App Development Project by Vatsal Sharma (Reg. No. 2427030235).")
        InfoCard("Photo credits", "MUJ demo listing photos from Wikimedia Commons: Biswarup Ganguly (CC BY 3.0); Sranjanm2002, LRBurdak (CC BY-SA 3.0); Tarunsamanta, Fredericknoronha (CC BY-SA 4.0). Details: docs/PHOTO_CREDITS.md")
        VSpace(8.dp)
        YellowButton(
            text = "Write an Email",
            onClick = {
                // An "Intent" asks another app (the email app) to do something for us
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Parkora support")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No email app found on this phone", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    InfoScreenLayout(title = "Privacy Policy", onBack = onBack) {
        InfoCard("What we store", "Your Google name, email and profile photo, the phone number and location you add, the parking spots you list, your bookings and in-app notifications.")
        InfoCard("Where it is stored", "In Google Firebase (Cloud Firestore), Parkora's online database. The Android app and the website parkora-tan.vercel.app use the same data.")
        InfoCard("Sign-in", "You sign in with Google. Parkora never sees or stores your Google password.")
        InfoCard("Photos and documents", "Parking photos are compressed and saved with your listing. For ownership documents only the file name is recorded.")
        InfoCard("Who can see your data", "Everyone can see parking listings. Spot owners see the name on bookings for their spots. Your phone number is not shown to other users. The Parkora admin can see all accounts, listings and bookings to keep the platform safe.")
        InfoCard("Deleting your data", "Contact us (Contact Us screen) and the admin will delete your account data.")
    }
}

/** Shared layout: yellow header + scrolling column of cards. */
@Composable
private fun InfoScreenLayout(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(title = title, navigationIcon = Icons.AutoMirrored.Filled.ArrowBack, onNavigationClick = onBack)
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    CreamCard(cornerRadius = 10.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
            VSpace(4.dp)
            Text(body, fontSize = 14.sp, color = TextGray, lineHeight = 20.sp)
        }
    }
}
