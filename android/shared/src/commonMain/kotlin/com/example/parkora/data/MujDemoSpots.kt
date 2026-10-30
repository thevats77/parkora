package com.example.parkora.data

import com.example.parkora.utils.nowMillis

/*
 * MUJ DEMO SPOTS
 * --------------
 * Four sample parking spots around Manipal University Jaipur (Dehmi Kalan,
 * Jaipur–Ajmer Expressway). The admin adds them to the live database ONCE with
 * "Add MUJ demo spots" in the Admin Portal. They use fixed ids, so pressing the
 * button again just updates them instead of creating duplicates.
 * They are clearly marked as demo listings owned by the Parkora admin.
 * Photos: real free-licensed photos from Wikimedia Commons (see docs/PHOTO_CREDITS.md).
 * (The website has the same list in web/src/lib/mujDemoSpots.ts.)
 */
object MujDemoSpots {

    fun spots(adminId: String): List<ParkingSpot> {
        val now = nowMillis()
        fun spot(
            id: String, number: String, society: String, address: String, type: String,
            length: Int, width: Int, vehicles: List<String>, images: List<String>,
            perHour: Int, perDay: Int, rating: Int, ageMinutes: Int
        ) = ParkingSpot(
            id = id, ownerId = adminId, ownerName = "Parkora (demo listing)",
            parkingNumber = number, societyName = society, address = address, city = "Jaipur",
            parkingType = type, lengthFeet = length, widthFeet = width, vehicleTypes = vehicles,
            imagePaths = images, documentNames = emptyList(), documentPaths = emptyList(),
            pricePerHour = perHour, pricePerDay = perDay, availableForRent = true, availableForSale = false,
            isAvailable = true, isVerified = true, rating = rating, createdAt = now - ageMinutes * 60_000L
        )

        return listOf(
            spot("muj_demo_1", "DK-12", "Dehmi Kalan Village Parking", "Near MUJ Main Gate, Dehmi Kalan",
                "Open", 18, 9, listOf("SUV", "Sedan", "Regular", "Two Wheeler"), listOf("demo_muj_dk12"), 20, 150, 4, 1),
            spot("muj_demo_2", "AE-04", "Ajmer Road Student Residency", "Jaipur–Ajmer Expressway (NH-48), 1 km from MUJ",
                "Open", 17, 8, listOf("SUV", "Sedan", "Regular", "Two Wheeler"), listOf("demo_muj_ae04", "demo_muj_ae04_road"), 30, 220, 5, 2),
            spot("muj_demo_3", "BG-21", "Bagru Heights Apartments", "Basement B1, Bagru Road, 3 km from MUJ",
                "Basement", 16, 8, listOf("Sedan", "Regular", "Two Wheeler"), listOf("demo_muj_bg21"), 25, 180, 4, 3),
            spot("muj_demo_4", "TW-07", "Dehmi Kalan Market Stand", "Market Road, Dehmi Kalan (opp. MUJ hostels)",
                "Open", 8, 4, listOf("Two Wheeler"), listOf("demo_muj_tw07"), 10, 60, 4, 4)
        )
    }
}
