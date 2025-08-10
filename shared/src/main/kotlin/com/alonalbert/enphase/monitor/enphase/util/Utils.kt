package com.alonalbert.enphase.monitor.enphase.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.FormatStyle.MEDIUM
import java.util.concurrent.TimeUnit
import kotlin.math.abs

val Double.kwh get() = "%.2f kWh".format(this)
val Double.kw get() = "%.2f kW".format(this)
val Double.round1 get() = "%.1f".format(this)
val Double.round2 get() = "%.2f".format(this)

operator fun LocalDate.plus(days: Int): LocalDate = plusDays(days.toLong())

operator fun LocalDate.minus(days: Int): LocalDate = minusDays(days.toLong())

fun LocalDate.toText(): String = format(ISO_LOCAL_DATE)

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

fun LocalDate.formatMedium(): String = format(DateTimeFormatter.ofLocalizedDate(MEDIUM))

fun LocalDate.format(): String = format(ISO_LOCAL_DATE)

fun Double.zerofy() = if (abs(this) < 0.01) 0.0 else this

fun LocalDate.toEpochMillis() = TimeUnit.DAYS.toMillis(toEpochDay())
