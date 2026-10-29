package com.example.parkora.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.FieldBox
import com.example.parkora.ui.components.FieldLabel
import com.example.parkora.ui.components.ParkoraTextField
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.TextGray
import com.example.parkora.utils.Validation
import kotlinx.coroutines.launch

/*
 * EDIT PROFILE – change name, phone and location.
 * The email is the login id, so it is shown but cannot be edited.
 * The boxes start with the current values (remember { mutableStateOf(user.name) }).
 */
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = ParkoraRepository.currentUser ?: return

    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var location by remember { mutableStateOf(user.location) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite).imePadding()) {
        YellowHeader(title = "Edit Profile", navigationIcon = Icons.AutoMirrored.Filled.ArrowBack, onNavigationClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            VSpace(8.dp)
            ParkoraTextField(name, { name = it; nameError = null }, "Full Name", "Enter your name", Icons.Filled.Person, error = nameError)
            ParkoraTextField(phone, { if (it.length <= 10) phone = it; phoneError = null }, "Phone Number", "10-digit mobile number",
                Icons.Filled.Phone, error = phoneError, keyboardType = KeyboardType.Phone)
            ParkoraTextField(location, { location = it; locationError = null }, "Location", "Area, City, Country",
                Icons.Filled.PersonPin, error = locationError)

            FieldLabel("Email (cannot be changed)")
            FieldBox(icon = Icons.Filled.Email) { Text(user.email, color = TextGray, fontSize = 15.sp) }
        }

        YellowButton(
            text = if (isSaving) "Saving…" else "Save Changes",
            enabled = !isSaving,
            onClick = {
                nameError = Validation.validateName(name)
                phoneError = Validation.validatePhone(phone)
                locationError = Validation.required(location, "your location")
                if (nameError == null && phoneError == null && locationError == null) {
                    isSaving = true
                    scope.launch {
                        val error = ParkoraRepository.updateProfile(name, phone, location)
                        isSaving = false
                        Toast.makeText(context, error ?: "Profile updated", Toast.LENGTH_SHORT).show()
                        if (error == null) onBack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp)
        )
    }
}
