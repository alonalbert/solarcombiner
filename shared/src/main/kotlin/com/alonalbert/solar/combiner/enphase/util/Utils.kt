package com.alonalbert.solar.combiner.enphase.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

val Double.kwh get() = "%.2f kWh".format(this)

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
