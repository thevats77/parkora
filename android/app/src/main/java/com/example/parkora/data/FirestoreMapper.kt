package com.example.parkora.data

import com.google.firebase.firestore.DocumentSnapshot

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
// Numbers come back from Firestore as Long, so we use getLong(...).toInt() for Int fields.

private fun DocumentSnapshot.str(field: String) = getString(field) ?: ""
private fun DocumentSnapshot.long(field: String) = getLong(field) ?: 0L
private fun DocumentSnapshot.bool(field: String) = getBoolean(field) ?: false

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.strings(field: String) = (get(field) as? List<String>) ?: emptyList()

fun DocumentSnapshot.toUser() = User(
    id = id, name = str("name"), email = str("email"), phone = str("phone"), location = str("location"),
    createdAt = long("createdAt"), profileImagePath = getString("profileImagePath"),
    role = getString("role") ?: User.ROLE_USER, isBlocked = bool("isBlocked")
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toParkingSpot(): ParkingSpot {
    val slots = (get("bookedSlots") as? List<Map<String, Any>>) ?: emptyList()
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
        bookedSlots = slots.map {
            BookedSlot(
                bookingId = it["bookingId"] as? String ?: "",
                startMillis = (it["startMillis"] as? Number)?.toLong() ?: 0L,
                endMillis = (it["endMillis"] as? Number)?.toLong() ?: 0L
            )
        }
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
