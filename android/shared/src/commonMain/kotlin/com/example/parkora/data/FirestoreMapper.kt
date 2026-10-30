package com.example.parkora.data

import dev.gitlive.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.Serializable

/*
 * FIRESTORE MAPPER
 * ----------------
 * Firestore stores each record as a "document" = a map of field name -> value.
 * These functions convert our Kotlin data classes into maps (to save them)
 * and documents back into data classes (after reading them).
 *
 * We write the conversion by hand (instead of Firestore's automatic toObject())
 * so the field names are exactly the same as on the website, e.g. "isVerified".
 */

// ---------------- Kotlin object -> map (for saving) ----------------

fun User.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "name" to name, "email" to email, "phone" to phone, "location" to location,
    "createdAt" to createdAt, "profileImagePath" to profileImagePath, "role" to role, "isBlocked" to isBlocked
)

fun BookedSlot.toMap(): Map<String, Any> =
    mapOf("bookingId" to bookingId, "startMillis" to startMillis, "endMillis" to endMillis)

fun ParkingSpot.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "ownerId" to ownerId, "ownerName" to ownerName,
    "parkingNumber" to parkingNumber, "societyName" to societyName, "address" to address, "city" to city,
    "parkingType" to parkingType, "lengthFeet" to lengthFeet, "widthFeet" to widthFeet, "vehicleTypes" to vehicleTypes,
    "imagePaths" to imagePaths, "documentNames" to documentNames, "documentPaths" to documentPaths,
    "pricePerHour" to pricePerHour, "pricePerDay" to pricePerDay,
    "availableForRent" to availableForRent, "availableForSale" to availableForSale,
    "isAvailable" to isAvailable, "isVerified" to isVerified, "rating" to rating, "createdAt" to createdAt,
    "bookedSlots" to bookedSlots.map { it.toMap() }
)

fun Booking.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "spotId" to spotId, "parkingNumber" to parkingNumber, "spotImage" to spotImage,
    "userId" to userId, "userName" to userName, "ownerId" to ownerId,
    "checkInMillis" to checkInMillis, "checkOutMillis" to checkOutMillis,
    "billedUnits" to billedUnits, "billedUnitName" to billedUnitName, "unitPrice" to unitPrice,
    "totalPrice" to totalPrice, "status" to status.name, "createdAt" to createdAt
)

fun AppNotification.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "userId" to userId, "title" to title, "message" to message, "isRead" to isRead, "createdAt" to createdAt
)

// ---------------- Firestore document -> Kotlin object (after reading) ----------------
// Each field is read with get<Type>(name). A missing or wrong field gives a default value
// instead of crashing (e.g. an old document without "isBlocked").

private inline fun <reified T> DocumentSnapshot.field(name: String): T? = runCatching { get<T?>(name) }.getOrNull()
private fun DocumentSnapshot.str(name: String) = field<String>(name) ?: ""
private fun DocumentSnapshot.long(name: String): Long = field<Long>(name) ?: field<Double>(name)?.toLong() ?: 0L
private fun DocumentSnapshot.bool(name: String) = field<Boolean>(name) ?: false
private fun DocumentSnapshot.strings(name: String) = field<List<String>>(name) ?: emptyList()

/** How one booked time slot is stored inside a parking-spot document. */
@Serializable
private data class SlotFields(val bookingId: String = "", val startMillis: Long = 0, val endMillis: Long = 0)

fun DocumentSnapshot.toUser() = User(
    id = id, name = str("name"), email = str("email"), phone = str("phone"), location = str("location"),
    createdAt = long("createdAt"), profileImagePath = field<String>("profileImagePath"),
    role = field<String>("role") ?: User.ROLE_USER, isBlocked = bool("isBlocked")
)

fun DocumentSnapshot.toParkingSpot(): ParkingSpot {
    val slots = field<List<SlotFields>>("bookedSlots") ?: emptyList()
    return ParkingSpot(
        id = id, ownerId = str("ownerId"), ownerName = str("ownerName"),
        parkingNumber = str("parkingNumber"), societyName = str("societyName"), address = str("address"), city = str("city"),
        parkingType = str("parkingType"), lengthFeet = long("lengthFeet").toInt(), widthFeet = long("widthFeet").toInt(),
        vehicleTypes = strings("vehicleTypes"), imagePaths = strings("imagePaths"),
        documentNames = strings("documentNames"), documentPaths = strings("documentPaths"),
        pricePerHour = long("pricePerHour").toInt(), pricePerDay = long("pricePerDay").toInt(),
        availableForRent = bool("availableForRent"), availableForSale = bool("availableForSale"),
        isAvailable = bool("isAvailable"), isVerified = bool("isVerified"), rating = long("rating").toInt(),
        createdAt = long("createdAt"),
        bookedSlots = slots.map { BookedSlot(it.bookingId, it.startMillis, it.endMillis) }
    )
}

fun DocumentSnapshot.toBooking() = Booking(
    id = id, spotId = str("spotId"), parkingNumber = str("parkingNumber"), spotImage = str("spotImage"),
    userId = str("userId"), userName = str("userName"), ownerId = str("ownerId"),
    checkInMillis = long("checkInMillis"), checkOutMillis = long("checkOutMillis"),
    billedUnits = long("billedUnits").toInt(), billedUnitName = str("billedUnitName"),
    unitPrice = long("unitPrice").toInt(), totalPrice = long("totalPrice").toInt(),
    status = runCatching { BookingStatus.valueOf(str("status")) }.getOrDefault(BookingStatus.PENDING),
    createdAt = long("createdAt")
)

fun DocumentSnapshot.toNotification() = AppNotification(
    id = id, userId = str("userId"), title = str("title"), message = str("message"),
    isRead = bool("isRead"), createdAt = long("createdAt")
)
