package com.example.parkora.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.components.EmptyState
import com.example.parkora.ui.components.ParkingCard
import com.example.parkora.ui.components.ParkoraTextField
import com.example.parkora.ui.components.YellowHeader
import com.example.parkora.ui.theme.ScreenWhite

/*
 * ALL PARKING SCREEN – opened with "View All" on the Home screen.
 * Shows every available spot with a simple search box
 * (matches parking number, society, address, city or vehicle type).
 */
@Composable
fun AllParkingScreen(onBack: () -> Unit, onSpotClick: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    val query = searchText.trim().lowercase()
    val spots = ParkoraRepository.latestSpots().filter { spot ->
        query.isEmpty() ||
            spot.parkingNumber.lowercase().contains(query) ||
            spot.societyName.lowercase().contains(query) ||
            spot.address.lowercase().contains(query) ||
            spot.city.lowercase().contains(query) ||
            spot.vehicleTypes.any { it.lowercase().contains(query) }
    }

    Column(modifier = Modifier.fillMaxSize().background(ScreenWhite)) {
        YellowHeader(title = "All Parking", navigationIcon = Icons.AutoMirrored.Filled.ArrowBack, onNavigationClick = onBack)

        ParkoraTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = "Search",
            placeholder = "Parking no., society, city or vehicle",
            icon = Icons.Filled.Search,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (spots.isEmpty()) {
            EmptyState("No parking spots match \"$searchText\"")
        } else {
            LazyColumn(
                modifier = Modifier.navigationBarsPadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(spots, key = { it.id }) { spot ->
                    ParkingCard(spot = spot, onClick = { onSpotClick(spot.id) }, onBookClick = { onSpotClick(spot.id) })
                }
            }
        }
    }
}
