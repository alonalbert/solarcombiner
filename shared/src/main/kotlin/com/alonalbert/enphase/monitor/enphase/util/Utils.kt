package com.alonalbert.enphase.monitor.enphase.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.FormatStyle.MEDIUM
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private val YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy")
private val TIME_OF_DAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")

val Double.kw get() = "%.2f kW".format(this)
val Double.round1 get() = "%.1f".format(this)
val Double.round2 get() = "%.2f".format(this)

operator fun LocalDate.plus(days: Int): LocalDate = plusDays(days.toLong())

operator fun LocalDate.minus(days: Int): LocalDate = minusDays(days.toLong())

infix operator fun LocalDate.rangeUntil(other: LocalDate): Sequence<LocalDate> {
  return sequence {
    var current = this@rangeUntil
    while (current < other) {
      yield(current)
      current += 1
    }
  }
}

infix operator fun LocalDate.rangeTo(other: LocalDate): Sequence<LocalDate> {
  return sequence {
    var current = this@rangeTo
    while (current <= other) {
      yield(current)
      current += 1
    }
  }
}

fun YearMonth.format(): String = format(YEAR_MONTH_FORMATTER)

fun LocalDate.formatMedium(): String = format(DateTimeFormatter.ofLocalizedDate(MEDIUM))

fun LocalDateTime.format(): String = format(TIME_OF_DAY_FORMATTER)

fun LocalTime.format(): String = format(TIME_OF_DAY_FORMATTER)

fun LocalDate.format(): String = format(ISO_LOCAL_DATE)

fun Double.zerofy() = if (abs(this) < 0.01) 0.0 else this

fun LocalDate.toEpochMillis() = TimeUnit.DAYS.toMillis(toEpochDay())

fun rangeOfChunk(chunk: Int): String {
  val startTime = timeOfMin(chunk * 15)
  val endTime = startTime.plusMinutes(15)
  return "${startTime.format()} - ${endTime.format()}"
}

fun timeOfMin(minute: Int): LocalTime = LocalTime.of(minute / 60, minute % 60)
