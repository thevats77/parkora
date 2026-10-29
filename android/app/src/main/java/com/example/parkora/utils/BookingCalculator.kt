package com.example.parkora.utils

import kotlin.math.ceil

/*
 * BOOKING PRICE CALCULATION
 * -------------------------
 * Rule used by Parkora (same as the screenshot in the synopsis):
 *   - Count the hours between check-in and check-out (a started hour counts as a full hour).
 *   - If the stay is LESS than 24 hours  -> charge per HOUR.
 *   - If the stay is 24 hours or MORE    -> charge per DAY (a started day counts as a full day).
 *
 * Example from the synopsis screenshot:
 *   22-09-2025 7:00 AM  ->  30-09-2025 11:00 PM  = 208 hours = 8.67 days -> 9 days
 *   9 days x ₹15/day = ₹135
 */

/** The result of the calculation, shown in the "Summary" box on the details screen. */
data class PriceSummary(
    val totalHours: Int,
    val billedUnits: Int,      // how many hours or days we charge for
    val isDaily: Boolean,      // true = charged per day, false = charged per hour
    val unitPrice: Int,        // price of one hour or one day
    val totalPrice: Int
) {
    val unitName: String get() = if (isDaily) "Day(s)" else "Hour(s)"
    val shortUnit: String get() = if (isDaily) "day" else "hr"
}

object BookingCalculator {

    /**
     * Returns the price summary, or null if check-out is not after check-in.
     */
    fun calculate(checkInMillis: Long, checkOutMillis: Long, pricePerHour: Int, pricePerDay: Int): PriceSummary? {
        val durationMillis = checkOutMillis - checkInMillis
        if (durationMillis <= 0) return null

        val millisInOneHour = 60 * 60 * 1000.0
        val totalHours = ceil(durationMillis / millisInOneHour).toInt()

        return if (totalHours < 24) {
            PriceSummary(
                totalHours = totalHours,
                billedUnits = totalHours,
                isDaily = false,
                unitPrice = pricePerHour,
                totalPrice = totalHours * pricePerHour
            )
        } else {
            val totalDays = ceil(totalHours / 24.0).toInt()
            PriceSummary(
                totalHours = totalHours,
                billedUnits = totalDays,
                isDaily = true,
                unitPrice = pricePerDay,
                totalPrice = totalDays * pricePerDay
            )
        }
    }

    /** Turns 208 hours into "8 days 16 hours" for display. */
    fun durationText(totalHours: Int): String {
        val days = totalHours / 24
        val hours = totalHours % 24
        return when {
            days == 0 -> "$hours hour(s)"
            hours == 0 -> "$days day(s)"
            else -> "$days day(s) $hours hour(s)"
        }
    }
}
