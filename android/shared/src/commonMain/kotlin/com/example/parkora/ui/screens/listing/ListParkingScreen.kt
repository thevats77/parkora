@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.example.parkora.ui.screens.listing

import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.name
import androidx.compose.ui.backhandler.BackHandler
import com.example.parkora.ui.components.showToast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkora.data.ParkingSpot
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.CreamCard
import com.example.parkora.ui.components.ErrorText
import com.example.parkora.ui.components.FieldLabel
import com.example.parkora.ui.components.OutlineButton
import com.example.parkora.ui.components.ParkingImage
import com.example.parkora.ui.components.ParkoraTextField
import com.example.parkora.ui.components.SelectChip
import com.example.parkora.ui.components.StepIndicator
import com.example.parkora.ui.components.VSpace
import com.example.parkora.ui.components.YellowButton
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.CardCream
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.theme.ScreenWhite
import com.example.parkora.ui.theme.TextGray
import com.example.parkora.utils.ImageUtils
import com.example.parkora.utils.Validation
import kotlinx.coroutines.launch

/*
 * LIST PARKING SPOT – 4-STEP FORM  (Figure 3 + section 9 of the synopsis)
 * ----------------------------------------------------------------------
 *   Step 1: Location/Address   (parking number, society, address, city)
 *   Step 2: Parking Size       (type, length × width, suitable vehicles)
 *   Step 3: Upload Documents   (parking photos + ownership documents)
 *   Step 4: Pricing            (per hour, per day, rent/sale, availability)
 *
 * All the answers are kept in state variables in THIS function, so they are
 * not lost when the user moves between steps. "Next" validates the current
 * step before moving on. On the last step, "Submit" saves the spot.
 */

val parkingTypes = listOf("Open", "Covered", "Basement")
val vehicleTypeOptions = listOf("SUV", "Sedan", "Regular", "Two Wheeler")

@Composable
fun ListParkingScreen(onClose: () -> Unit, onSubmitted: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1
    var parkingNumber by remember { mutableStateOf("") }
    var societyName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    // Step 2
    var parkingType by remember { mutableStateOf("") }
    var lengthFeet by remember { mutableStateOf("") }
    var widthFeet by remember { mutableStateOf("") }
    val selectedVehicles = remember { mutableStateListOf<String>() }
    // Step 3
    val imagePaths = remember { mutableStateListOf<String>() }
    val documentNames = remember { mutableStateListOf<String>() }
    val documentPaths = remember { mutableStateListOf<String>() }
    // Step 4
    var pricePerHour by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf("") }
    var forRent by remember { mutableStateOf(true) }
    var forSale by remember { mutableStateOf(false) }
    var availableNow by remember { mutableStateOf(true) }

    // Error messages: field name -> message. Typing in a field removes its message.
    val errors = remember { mutableStateMapOf<String, String>() }

    val scope = rememberCoroutineScope()
    // ----- Pickers for photos and documents (FileKit library: works on Android and iPhone) -----
    // Photos are shrunk and stored as text inside the Firestore document (see ImageUtils.kt)
    val photoPicker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file != null) scope.launch {
            val photo = ImageUtils.bytesToDataUrl(file.readBytes())
            if (photo != null) imagePaths.add(photo)
            else showToast("Could not read that photo")
        }
    }
    // For documents only the file NAME is saved (uploading files needs paid Firebase Storage)
    val documentPicker = rememberFilePickerLauncher(type = FileKitType.File("pdf", "jpg", "jpeg", "png")) { file ->
        if (file != null) documentNames.add(file.name)
    }
    var isSubmitting by remember { mutableStateOf(false) }

    /** Checks the fields of the current step. Returns true when everything is OK. */
    fun validateStep(step: Int): Boolean {
        errors.clear()
        when (step) {
            1 -> {
                Validation.required(parkingNumber, "the parking number")?.let { errors["parkingNumber"] = it }
                Validation.required(societyName, "the society name")?.let { errors["societyName"] = it }
                Validation.required(address, "the address")?.let { errors["address"] = it }
                Validation.required(city, "the city")?.let { errors["city"] = it }
            }
            2 -> {
                if (parkingType.isEmpty()) errors["parkingType"] = "Please choose a parking type"
                Validation.positiveNumber(lengthFeet, "Length")?.let { errors["length"] = it }
                Validation.positiveNumber(widthFeet, "Width")?.let { errors["width"] = it }
                if (selectedVehicles.isEmpty()) errors["vehicles"] = "Choose at least one vehicle type"
            }
            3 -> {
                // Photos and documents are optional in this demo (see the note on the screen)
            }
            4 -> {
                Validation.positiveNumber(pricePerHour, "Price per hour")?.let { errors["pricePerHour"] = it }
                Validation.positiveNumber(pricePerDay, "Price per day")?.let { errors["pricePerDay"] = it }
                if (!forRent && !forSale) errors["availableFor"] = "Choose Rent, Sale or both"
            }
        }
        return errors.isEmpty()
    }

    fun submitListing() {
        val spot = ParkingSpot(
            id = "",            // the repository creates the real id, owner and date
            ownerId = "",
            ownerName = "",
            parkingNumber = parkingNumber.trim().uppercase(),
            societyName = societyName.trim(),
            address = address.trim(),
            city = city.trim(),
            parkingType = parkingType,
            lengthFeet = lengthFeet.toInt(),
            widthFeet = widthFeet.toInt(),
            vehicleTypes = vehicleTypeOptions.filter { it in selectedVehicles }, // keeps a fixed order
            imagePaths = imagePaths.toList(),
            documentNames = documentNames.toList(),
            documentPaths = documentPaths.toList(),
            pricePerHour = pricePerHour.toInt(),
            pricePerDay = pricePerDay.toInt(),
            availableForRent = forRent,
            availableForSale = forSale,
            isAvailable = availableNow,
            isVerified = false, // new listings wait until the admin verifies them in the Admin Portal
            rating = 0,
            createdAt = 0
        )
        isSubmitting = true
        scope.launch {
            val error = ParkoraRepository.addParkingSpot(spot)
            isSubmitting = false
            if (error != null) {
                showToast(error)
            } else {
                showToast("Parking spot ${spot.parkingNumber} is now live!")
                onSubmitted()
            }
        }
    }

    // The phone's back button goes to the previous step instead of closing the form
    BackHandler(enabled = currentStep > 1) {
        errors.clear()
        currentStep--
    }

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite).imePadding()) {
        YellowHeader(title = "List Parking Spot", navigationIcon = Icons.Filled.Close, onNavigationClick = onClose)
        StepIndicator(currentStep = currentStep, modifier = Modifier.padding(horizontal = 8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            when (currentStep) {
                1 -> {
                    StepTitle("Select Location/Address")
                    ParkoraTextField(parkingNumber, { parkingNumber = it; errors.remove("parkingNumber") }, "Parking Number", "Please specify the parking number",
                        Icons.Filled.LocalParking, error = errors["parkingNumber"])
                    ParkoraTextField(societyName, { societyName = it; errors.remove("societyName") }, "Society Name", "e.g. Prateek Grand City",
                        Icons.Filled.Apartment, error = errors["societyName"])
                    ParkoraTextField(address, { address = it; errors.remove("address") }, "Address", "Tower / block, street, area",
                        Icons.Filled.Home, error = errors["address"])
                    ParkoraTextField(city, { city = it; errors.remove("city") }, "City", "e.g. Ghaziabad",
                        Icons.Filled.LocationCity, error = errors["city"])
                }

                2 -> {
                    StepTitle("Parking Size")
                    FieldLabel("Parking Type")
                    ChipGroup(options = parkingTypes, isSelected = { it == parkingType }, onClick = { parkingType = it; errors.remove("parkingType") })
                    ErrorText(errors["parkingType"])

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ParkoraTextField(lengthFeet, { lengthFeet = it.filter(Char::isDigit); errors.remove("length") }, "Length (feet)", "e.g. 16",
                            Icons.Filled.Straighten, error = errors["length"], keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                        ParkoraTextField(widthFeet, { widthFeet = it.filter(Char::isDigit); errors.remove("width") }, "Width (feet)", "e.g. 8",
                            Icons.Filled.Straighten, error = errors["width"], keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                    }
                    Text("Tip: a normal car needs about 16 × 8 feet.", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(start = 6.dp, top = 6.dp))

                    FieldLabel("Suitable Vehicles (choose one or more)")
                    ChipGroup(
                        options = vehicleTypeOptions,
                        isSelected = { it in selectedVehicles },
                        onClick = {
                            if (it in selectedVehicles) selectedVehicles.remove(it) else selectedVehicles.add(it)
                            errors.remove("vehicles")
                        }
                    )
                    ErrorText(errors["vehicles"])
                }

                3 -> {
                    StepTitle("Upload Documents")
                    FieldLabel("Parking Photos")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        imagePaths.forEach { path ->
                            Box {
                                ParkingImage(path, Modifier.size(96.dp).clip(RoundedCornerShape(10.dp)))
                                RemoveButton(onClick = { imagePaths.remove(path) }, modifier = Modifier.align(Alignment.TopEnd))
                            }
                        }
                        if (imagePaths.size < 3) { // max 3 photos so the spot fits in one Firestore document
                            AddTile(icon = Icons.Filled.AddAPhoto, text = "Add Photo") {
                                photoPicker.launch()
                            }
                        }
                    }

                    FieldLabel("Ownership / Authorization Documents")
                    documentNames.forEachIndexed { index, name ->
                        CreamCard(cornerRadius = 10.dp, modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = DarkNavy)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(name, fontSize = 14.sp, color = DarkNavy, modifier = Modifier.weight(1f), maxLines = 1)
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextGray,
                                    modifier = Modifier.clickable {
                                        documentNames.removeAt(index)
                                    })
                            }
                        }
                    }
                    AddTile(icon = Icons.Filled.UploadFile, text = "Upload Document (PDF or image)", wide = true) {
                        documentPicker.launch()
                    }
                    VSpace(12.dp)
                    Text(
                        "Optional. Up to 3 photos are compressed and saved online with your listing. For documents only the " +
                            "file name is recorded. New listings show \"Verification pending\" until the Parkora admin verifies them.",
                        fontSize = 12.sp, color = TextGray
                    )
                }

                4 -> {
                    StepTitle("Pricing")
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ParkoraTextField(pricePerHour, { pricePerHour = it.filter(Char::isDigit); errors.remove("pricePerHour") }, "Price per Hour (₹)", "e.g. 40",
                            Icons.Filled.CurrencyRupee, error = errors["pricePerHour"], keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                        ParkoraTextField(pricePerDay, { pricePerDay = it.filter(Char::isDigit); errors.remove("pricePerDay") }, "Price per Day (₹)", "e.g. 300",
                            Icons.Filled.CurrencyRupee, error = errors["pricePerDay"], keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                    }
                    FieldLabel("Available For")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SelectChip("Rent", forRent) { forRent = !forRent; errors.remove("availableFor") }
                        SelectChip("Sale", forSale) { forSale = !forSale; errors.remove("availableFor") }
                    }
                    ErrorText(errors["availableFor"])

                    VSpace(16.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Available for booking now", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkNavy)
                            Text("You can switch this off later in My Parking Spots", fontSize = 12.sp, color = TextGray)
                        }
                        Switch(
                            checked = availableNow,
                            onCheckedChange = { availableNow = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = ParkoraYellow, checkedThumbColor = DarkNavy)
                        )
                    }

                    // Short review of everything entered so far
                    VSpace(16.dp)
                    CreamCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Review", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkNavy)
                            VSpace(6.dp)
                            ReviewLine("Parking", "${parkingNumber.uppercase()}, $societyName")
                            ReviewLine("Address", "$address, $city")
                            ReviewLine("Size", "$parkingType, $lengthFeet × $widthFeet ft")
                            ReviewLine("Vehicles", selectedVehicles.joinToString(", "))
                            ReviewLine("Photos / Docs", "${imagePaths.size} / ${documentNames.size}")
                        }
                    }
                }
            }
            VSpace(20.dp)
        }

        // ----- Buttons -----
        Row(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                OutlineButton(text = "Back", onClick = { errors.clear(); currentStep-- })
            }
            YellowButton(
                text = if (currentStep < 4) "Next" else if (isSubmitting) "Publishing…" else "Submit",
                enabled = !isSubmitting,
                onClick = {
                    if (validateStep(currentStep)) {
                        if (currentStep < 4) currentStep++ else submitListing()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(text, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = DarkNavy, modifier = Modifier.padding(top = 8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(options: List<String>, isSelected: (String) -> Boolean, onClick: (String) -> Unit) {
    // FlowRow puts chips on the next line when there is no more space
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option -> SelectChip(option, isSelected(option)) { onClick(option) } }
    }
}

@Composable
private fun AddTile(icon: ImageVector, text: String, wide: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    val sizeModifier = if (wide) Modifier.fillMaxWidth() else Modifier.size(96.dp)
    Column(
        modifier = sizeModifier
            .clip(shape)
            .background(CardCream)
            .border(1.dp, Color(0xFFE6D98C), shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = DarkNavy)
        Text(text, fontSize = 12.sp, color = DarkNavy, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun RemoveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .size(22.dp)
            .background(Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = DarkNavy, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun ReviewLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", fontSize = 13.sp, color = TextGray)
        Text(value, fontSize = 13.sp, color = DarkNavy)
    }
}
