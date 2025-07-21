package com.alonalbert.solar.combiner.enphase

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt

private const val YEAR = 2000
private const val MONTH = 1
private const val DAY = 2

object ReserveCalculator {
  fun calculateReserve(now: LocalTime, idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargeStart: Int): Int {
    val day = if (now.hour < chargeStart) DAY else DAY - 1
    val nowDateTime = LocalDateTime.of(YEAR, MONTH, day, now.hour, now.minute)
    val chargeStartDateTime = LocalDateTime.of(YEAR, MONTH, DAY, chargeStart, 0)
    val duration = Duration.between(nowDateTime, chargeStartDateTime)
    val minutes = duration.toMinutes()
    val min = batteryCapacity * minReserve / 100
    val needed = min + idleLoad * (minutes.toDouble() / 60)
    return (needed / batteryCapacity * 100).roundToInt().coerceAtMost(100)
  }
}