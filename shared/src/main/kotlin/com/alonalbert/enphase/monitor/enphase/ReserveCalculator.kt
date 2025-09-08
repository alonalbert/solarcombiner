package com.alonalbert.enphase.monitor.enphase

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt

private const val YEAR = 2000
private const val MONTH = 1
private const val DAY = 2

object ReserveCalculator {
  fun calculateReserve(
    time: LocalTime,
    idleLoad: Double,
    batteryCapacity: Double,
    minReserve: Int,
    chargeStart: Int,
    chargeEnd: Int,
  ): Int {
    if (time.hour in (chargeStart..chargeEnd)) {
      return minReserve
    }
    val day = if (time.hour < chargeStart) DAY else DAY - 1
    val dateTime = LocalDateTime.of(YEAR, MONTH, day, time.hour, time.minute)
    val chargeStartDateTime = LocalDateTime.of(YEAR, MONTH, DAY, chargeStart, 0)
    val duration = Duration.between(dateTime, chargeStartDateTime)
    val minutes = duration.toMinutes()
    val min = batteryCapacity * minReserve / 100
    val needed = min + idleLoad * (minutes.toDouble() / 60)
    return (needed / batteryCapacity * 100).roundToInt().coerceAtMost(100)
  }

  fun calculateDailyReserves(
    idleLoad: Double,
    batteryCapacity: Double,
    minReserve: Int,
    chargeStart: Int,
    chargeEnd: Int,
  ): List<Int> {
    return (0..95).map {
      val time = LocalTime.of(
        /* hour = */ it / 4,
        /* minute = */ 15 * (it % 4),
      )
      calculateReserve(time, idleLoad, batteryCapacity, minReserve, chargeStart, chargeEnd)
    }
  }
}