package com.example.parkora.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parkora.data.ParkoraRepository
import com.example.parkora.ui.screens.admin.AdminPortalScreen
import com.example.parkora.ui.screens.auth.LoginScreen
import com.example.parkora.ui.theme.DarkNavy
import com.example.parkora.ui.theme.ParkoraYellow
import com.example.parkora.ui.screens.bookings.BookingRequestsScreen
import com.example.parkora.ui.screens.bookings.MyBookingsScreen
import com.example.parkora.ui.screens.detail.ParkingDetailScreen
import com.example.parkora.ui.screens.home.AllParkingScreen
import com.example.parkora.ui.screens.home.HomeScreen
import com.example.parkora.ui.screens.listing.ListParkingScreen
import com.example.parkora.ui.screens.notifications.NotificationsScreen
import com.example.parkora.ui.screens.profile.ContactUsScreen
import com.example.parkora.ui.screens.profile.EditProfileScreen
import com.example.parkora.ui.screens.profile.PrivacyPolicyScreen
import com.example.parkora.ui.screens.profile.ProfileScreen
import com.example.parkora.ui.screens.spots.MyParkingSpotsScreen

/*
 * NAVIGATION GRAPH
 * ----------------
 * NavHost is a container that shows ONE screen at a time.
 * Each composable(route) { ... } line connects a route name to a screen.
 * Screens never navigate by themselves – they call lambdas such as
 * onSpotClick(id), and THIS file decides where to go. That keeps screens simple.
 */
@Composable
fun ParkoraNavGraph() {
    when {
        // Firebase is still checking whether someone is signed in from last time
        ParkoraRepository.isCheckingLogin -> LoadingScreen()
        // Nobody signed in -> Google login (after logout we come back here automatically)
        ParkoraRepository.currentUser == null -> LoginScreen()
        // Signed in -> the normal app screens
        else -> SignedInNavHost()
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(ParkoraYellow), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = DarkNavy)
    }
}

@Composable
private fun SignedInNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onSpotClick = { spotId -> navController.navigate(Routes.parkingDetail(spotId)) },
                onViewAllClick = { navController.navigate(Routes.ALL_PARKING) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onAddClick = { navController.navigate(Routes.LIST_PARKING) },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE) { launchSingleTop = true }
                }
            )
        }

        composable(Routes.ALL_PARKING) {
            AllParkingScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId -> navController.navigate(Routes.parkingDetail(spotId)) }
            )
        }

        // Screen with an argument: read "spotId" out of the route
        composable(Routes.PARKING_DETAIL) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getString("spotId") ?: ""
            ParkingDetailScreen(
                spotId = spotId,
                onBack = { navController.popBackStack() },
                onViewBookings = {
                    navController.popBackStack()
                    navController.navigate(Routes.MY_BOOKINGS)
                }
            )
        }

        composable(Routes.LIST_PARKING) {
            ListParkingScreen(
                onClose = { navController.popBackStack() },
                onSubmitted = {
                    navController.popBackStack()
                    navController.navigate(Routes.MY_SPOTS)
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onMySpotsClick = { navController.navigate(Routes.MY_SPOTS) },
                onMyBookingsClick = { navController.navigate(Routes.MY_BOOKINGS) },
                onBookingRequestsClick = { navController.navigate(Routes.BOOKING_REQUESTS) },
                onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                onContactUsClick = { navController.navigate(Routes.CONTACT_US) },
                onPrivacyPolicyClick = { navController.navigate(Routes.PRIVACY_POLICY) },
                onAdminPortalClick = { navController.navigate(Routes.ADMIN) },
                onHomeClick = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onAddClick = { navController.navigate(Routes.LIST_PARKING) }
            )
        }

        composable(Routes.MY_SPOTS) {
            MyParkingSpotsScreen(
                onBack = { navController.popBackStack() },
                onAddClick = { navController.navigate(Routes.LIST_PARKING) },
                onSpotClick = { spotId -> navController.navigate(Routes.parkingDetail(spotId)) }
            )
        }

        composable(Routes.MY_BOOKINGS) {
            MyBookingsScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId -> navController.navigate(Routes.parkingDetail(spotId)) }
            )
        }

        composable(Routes.BOOKING_REQUESTS) {
            BookingRequestsScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId -> navController.navigate(Routes.parkingDetail(spotId)) }
            )
        }

        composable(Routes.EDIT_PROFILE) { EditProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CONTACT_US) { ContactUsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PRIVACY_POLICY) { PrivacyPolicyScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.ADMIN) {
            AdminPortalScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId -> navController.navigate(Routes.parkingDetail(spotId)) }
            )
        }
    }
}
