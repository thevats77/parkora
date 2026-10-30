package com.example.parkora.ui.screens.auth

import com.example.parkora.resources.Res
import com.example.parkora.resources.ic_google
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.ErrorText
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.theme.CardBorder
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.ParkoraYellowLight
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.TextGray
import kotlinx.coroutines.launch

/*
 * LOGIN SCREEN – "Continue with Google"
 * -------------------------------------
 * There are no passwords in Parkora any more: everyone signs in with their Google
 * account (Firebase Authentication). The first sign-in automatically creates the
 * user's profile in the database. The admin signs in the same way – the account
 * set in FirebaseConfig.ADMIN_EMAIL simply gets the Admin Portal.
 *
 * After a successful sign-in we do not navigate from here: the repository's
 * login listener sets currentUser, and the navigation graph then shows Home.
 */
@Composable
fun LoginScreen() {
    val scope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenWhite)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        AuthHeader(subtitle = "Smart Peer-to-Peer Parking & Booking")

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            VSpace(32.dp)
            Text("Welcome!", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
            Text(
                "Find, book and list parking spaces around Manipal University Jaipur.",
                fontSize = 15.sp, color = TextGray
            )
            VSpace(32.dp)

            // ----- Continue with Google -----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(3.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .clickable(enabled = !isSigningIn) {
                        isSigningIn = true
                        error = null
                        scope.launch {
                            error = ParkoraRepository.signInWithGoogle()
                            isSigningIn = false
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = DarkNavy)
                } else {
                    Image(painterResource(Res.drawable.ic_google), contentDescription = null, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isSigningIn) "Signing in…" else "Continue with Google",
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy
                )
            }
            ErrorText(error ?: ParkoraRepository.loginError)

            if (!ParkoraRepository.isConfigured) {
                VSpace(16.dp)
                CreamCard {
                    Text(
                        "Firebase is not connected yet. Fill in data/FirebaseConfig.kt (see README).",
                        modifier = Modifier.padding(14.dp), fontSize = 13.sp, color = TextGray
                    )
                }
            }

            VSpace(28.dp)
            Text(
                "By continuing you agree that Parkora stores your name, email and bookings in its " +
                    "online database (Google Firebase). See the Privacy Policy in your profile.",
                fontSize = 12.sp, color = TextGray, lineHeight = 17.sp
            )
            VSpace(24.dp)
        }
    }
}

/** Big yellow header with the Parkora logo, shared by Login and Register. */
@Composable
fun AuthHeader(subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(ParkoraYellow)
            .drawBehind {
                drawCircle(ParkoraYellowLight, radius = size.width * 0.45f, center = Offset(size.width * 0.1f, 0f))
                drawCircle(ParkoraYellowLight.copy(alpha = 0.5f), radius = size.width * 0.3f, center = Offset(size.width * 0.95f, size.height))
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 28.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo: dark circle with a yellow "P"
            Box(
                modifier = Modifier.size(78.dp).background(DarkNavy, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("P", color = ParkoraYellow, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            }
            VSpace(12.dp)
            Text("PARKORA", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = DarkNavy, letterSpacing = 3.sp)
            Text(subtitle, fontSize = 14.sp, color = DarkNavy)
        }
    }
}
