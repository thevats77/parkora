package com.example.parkora.data

/*
 * DATA MODELS
 * -----------
 * A "data class" in Kotlin is a class whose main job is to hold data.
 * Kotlin automatically gives it equals(), toString() and copy().
 *
 * These four classes match the database design in the synopsis and the
 * four Cloud Firestore collections: users, parking_spots, bookings, notifications.
 * (FirestoreMapper.kt converts them to/from Firestore documents.)
 * Booking requests are not a separate class: a "booking request" is simply
 * a Booking whose ownerId is the logged-in user (see ParkoraRepository).
 */

/**
 * One person who signed in with Google. The same user can book parking AND list parking.
 * id = the Firebase Authentication uid. No password is stored – Google handles sign-in.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,                 // empty until the user adds it in Edit Profile
    val location: String,              // shown in the home header, e.g. "Dehmi Kalan, Jaipur"
    val createdAt: Long,
    val profileImagePath: String? = null, // Google photo URL, or a photo chosen with the camera icon
    val role: String = ROLE_USER,      // "user" or "admin"
    val isBlocked: Boolean = false     // set by the admin; blocked users cannot list or book
) {
    val isAdmin: Boolean get() = role == ROLE_ADMIN

    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ADMIN = "admin"
    }
}

/**
 * A time range that is already taken on a parking spot.
 * Stored inside the spot document so the app can check for double bookings
 * in ONE Firestore transaction (safe even if two people book at the same moment).
 */
data class BookedSlot(
    val bookingId: String,
    val startMillis: Long,
    val endMillis: Long
)

/** One parking space that an owner has listed using the 4-step form. */
data class ParkingSpot(
    val id: String,
    val ownerId: String,
    val ownerName: String,

    // Step 1 – Location / Address
    val parkingNumber: String,         // e.g. "C4-1007"
    val societyName: String,           // e.g. "Prateek Grand City"
    val address: String,               // e.g. "Tower-C5, Siddharth Vihar"
    val city: String,                  // e.g. "Ghaziabad"

    // Step 2 – Parking Size
    val parkingType: String,           // "Covered", "Open" or "Basement"
    val lengthFeet: Int,
    val widthFeet: Int,
    val vehicleTypes: List<String>,    // e.g. ["SUV", "Sedan", "Regular", "Two Wheeler"]

    // Step 3 – Upload Documents
    val imagePaths: List<String>,      // "demo_parking_1" (built-in picture) or a photo saved as "data:image/jpeg;base64,..."
    val documentNames: List<String>,   // names of ownership/authorization documents the owner attached
    val documentPaths: List<String>,

    // Step 4 – Pricing
    val pricePerHour: Int,
    val pricePerDay: Int,
    val availableForRent: Boolean,
    val availableForSale: Boolean,
    val isAvailable: Boolean,          // owner can switch the spot ON/OFF for bookings

    val isVerified: Boolean,           // demo spots are verified; new spots wait for verification
    val rating: Int,                   // 0 to 5 stars (0 = new spot, no rating yet)
    val createdAt: Long,
    val bookedSlots: List<BookedSlot> = emptyList()  // active (pending/approved) bookings
)

/** All the states a booking can be in. */
enum class BookingStatus(val label: String) {
    PENDING("Pending"),       // user booked, owner has not answered yet
    APPROVED("Approved"),     // owner accepted
    REJECTED("Rejected"),     // owner declined
    CANCELLED("Cancelled"),   // user cancelled
    COMPLETED("Completed"),   // approved booking whose check-out time has passed
    EXPIRED("Expired")        // pending booking that the owner never answered in time
}

/** One booking made by a user for a parking spot. */
data class Booking(
    val id: String,
    val spotId: String,
    val parkingNumber: String,         // copied here so we can still show it if the spot is deleted
    val spotImage: String,
    val userId: String,                // who booked
    val userName: String,
    val ownerId: String,               // who owns the spot (receives the booking request)
    val checkInMillis: Long,           // date + time stored as milliseconds
    val checkOutMillis: Long,
    val billedUnits: Int,              // e.g. 9
    val billedUnitName: String,        // "Hour(s)" or "Day(s)"
    val unitPrice: Int,                // price of one hour/day at the time of booking
    val totalPrice: Int,
    val status: BookingStatus,
    val createdAt: Long
)

/** A simple in-app message, e.g. "New booking request for C4-1007". */
data class AppNotification(
    val id: String,
    val userId: String,                // who should see this notification
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: Long
)
