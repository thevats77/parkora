package com.example.parkora.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** The current time in milliseconds (works on Android and iPhone). */
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/*
 * Small helper functions for dates and times.
 * We store date+time as a Long (milliseconds) because it is easy to save
 * and easy to compare. For showing it on screen we convert it to text.
 * (kotlinx-datetime is used because it works on both Android and iPhone.)
 */
object DateTimeUtils {

    private val zone get() = TimeZone.currentSystemDefault()
    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    private fun Int.twoDigits() = toString().padStart(2, '0')

    /** 22-09-2025 */
    fun formatDate(date: LocalDate): String = "${date.day.twoDigits()}-${date.month.number.twoDigits()}-${date.year}"

    /** 7:00 AM */
    fun formatTime(time: LocalTime): String {
        val hour12 = if (time.hour % 12 == 0) 12 else time.hour % 12
        return "$hour12:${time.minute.twoDigits()} ${if (time.hour < 12) "AM" else "PM"}"
    }

    /** Joins a date and a time into milliseconds, e.g. 22-09-2025 + 7:00 AM -> 1758504600000 */
    fun toMillis(date: LocalDate, time: LocalTime): Long =
        LocalDateTime(date, time).toInstant(zone).toEpochMilliseconds()

    fun toLocalDateTime(millis: Long): LocalDateTime =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(zone)

    /** Used on the My Bookings cards, e.g. "Sep 22, 25, 7:00 AM" */
    fun formatForBookingCard(millis: Long): String {
        val t = toLocalDateTime(millis)
        return "${months[t.month.number - 1]} ${t.day.twoDigits()}, ${(t.year % 100).twoDigits()}, ${formatTime(t.time)}"
    }

    /** e.g. "22 Sep, 7:00 AM" */
    fun formatForNotification(millis: Long): String {
        val t = toLocalDateTime(millis)
        return "${t.day.twoDigits()} ${months[t.month.number - 1]}, ${formatTime(t.time)}"
    }

    fun today(): LocalDate = toLocalDateTime(nowMillis()).date

    /** Rounds "now" up to the next full hour – a sensible default check-in time. */
    fun nextFullHour(): LocalDateTime {
        val now = toLocalDateTime(nowMillis())
        val startOfHour = LocalDateTime(now.date, LocalTime(now.hour, 0))
        return plusHours(startOfHour, 1)
    }

    fun plusHours(time: LocalDateTime, hours: Int): LocalDateTime =
        time.toInstant(zone).plus(hours, DateTimeUnit.HOUR, zone).toLocalDateTime(zone)
}
