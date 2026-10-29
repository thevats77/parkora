package com.example.parkora.navigation

/*
 * ROUTES
 * ------
 * Every screen has a unique text name called a "route" (like a web address).
 * To open a screen we call navController.navigate(Routes.SOMETHING).
 * (Login is not a route: ParkoraNavGraph shows it whenever nobody is signed in.)
 */
object Routes {
    const val HOME = "home"
    const val ALL_PARKING = "all_parking"
    const val PROFILE = "profile"
    const val LIST_PARKING = "list_parking"
    const val MY_SPOTS = "my_parking_spots"
    const val MY_BOOKINGS = "my_bookings"
    const val BOOKING_REQUESTS = "booking_requests"
    const val EDIT_PROFILE = "edit_profile"
    const val CONTACT_US = "contact_us"
    const val PRIVACY_POLICY = "privacy_policy"
    const val NOTIFICATIONS = "notifications"
    const val ADMIN = "admin_portal"

    // The details screen needs to know WHICH spot to show,
    // so its route contains an argument: parking_detail/spot_p5_1456
    const val PARKING_DETAIL = "parking_detail/{spotId}"
    fun parkingDetail(spotId: String) = "parking_detail/$spotId"
}
