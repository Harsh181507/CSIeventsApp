package com.example.csievent.presentation.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** "Aarav Kumar" -> "AK", "aarav" -> "A". */
fun initials(name: String): String =
    name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** First word of a name, for greetings. */
fun firstName(name: String?): String =
    name?.trim()?.split(Regex("\\s+"))?.firstOrNull()?.takeIf { it.isNotEmpty() } ?: "there"

private fun parseDate(date: String?): LocalDate? =
    try {
        date?.let { LocalDate.parse(it) }
    } catch (e: Exception) {
        null
    }

/** "2026-11-12" -> "12 Nov 2026". */
fun formatDate(date: String?): String =
    parseDate(date)?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) ?: "Date TBA"

/** "2026-11-12" -> "12 NOV". */
fun formatDayMonth(date: String?): String =
    parseDate(date)?.format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))?.uppercase() ?: "TBA"

/** 19.0 -> "19", 18.5 -> "18.5", 18.333 -> "18.33". */
fun formatScore(score: Double): String =
    if (score % 1.0 == 0.0) score.toLong().toString()
    else String.format(Locale.US, "%.2f", score).trimEnd('0').trimEnd('.')

/** "A3F9B2C1" -> "A3F9 B2C1" for easier reading aloud. */
fun formatJoinCode(code: String): String =
    if (code.length == 8) code.substring(0, 4) + " " + code.substring(4) else code

/** Time-of-day greeting. */
fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}
