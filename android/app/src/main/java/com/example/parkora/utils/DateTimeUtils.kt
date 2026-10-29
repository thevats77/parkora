package com.example.parkora.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Small helper functions for dates and times.
 * We store date+time as a Long (milliseconds) because it is easy to save
 * and easy to compare. For showing it on screen we convert it to text.
 */
object DateTimeUtils {

    private val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)           // 22-09-2025
    private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)               // 7:00 AM
    private val bookingCardFormat = DateTimeFormatter.ofPattern("MMM dd, yy, h:mm a", Locale.ENGLISH) // Sep 22, 25, 7:00 AM
    private val notificationFormat = DateTimeFormatter.ofPattern("dd MMM, h:mm a", Locale.ENGLISH)

    fun formatDate(date: LocalDate): String = date.format(dateFormat)

    fun formatTime(time: LocalTime): String = time.format(timeFormat)

    /** Joins a date and a time into milliseconds, e.g. 22-09-2025 + 7:00 AM -> 1758504600000 */
    fun toMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun toLocalDateTime(millis: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

    /** Used on the My Bookings cards, e.g. "Sep 22, 25, 7:00 AM" */
    fun formatForBookingCard(millis: Long): String = toLocalDateTime(millis).format(bookingCardFormat)

    fun formatForNotification(millis: Long): String = toLocalDateTime(millis).format(notificationFormat)

    /** Rounds "now" up to the next full hour – a sensible default check-in time. */
    fun nextFullHour(): LocalDateTime =
        LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1)
}
