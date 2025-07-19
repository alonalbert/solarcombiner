@file:JvmName("ReserveManager")
package com.alonalbert.solar.reservemanager

import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.util.DefaultLogger
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.math.roundToInt

private val logger = DefaultLogger()

fun main(args: Array<String>) {
  val parser = ArgParser("reserve-manager")

  val idleLoad by parser.option(ArgType.Double, shortName = "i", fullName = "idle-load", description = "Idle consumption (kWh)").required()
  val batteryCapacity by parser.option(ArgType.Double, shortName = "b", fullName = "battery-capacity", description = "Battery capacity (kWh)")
    .required()
  val minReserve by parser.option(ArgType.Int, shortName = "r", fullName = "min-reserve", description = "Min reserve %").default(20)
  val chargeStart by parser.option(ArgType.Int, shortName = "s", fullName = "charge-start", description = "Hour at which solar charging starts").default(9)
  val chargeEnd by parser.option(ArgType.Int, shortName = "e", fullName = "charge-end", description = "Hour at which solar charging ends").default(16)
  val testMode by parser.option(ArgType.Boolean, shortName = "t", fullName = "test-mode", description = "Print out list of actions").default(false)

  parser.parse(args)

  val chargeRange = chargeStart..chargeEnd

  when (testMode) {
    true -> runTest(idleLoad, batteryCapacity, minReserve, chargeRange)
    false -> setReserve(idleLoad, batteryCapacity, minReserve, chargeRange)
  }
}

private fun setReserve(idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargingRange: IntRange) {
  val reserve = calculateReserve(LocalTime.now(ZoneId.systemDefault()), idleLoad, batteryCapacity, minReserve, chargingRange)
  if (reserve == null) {
    logger.info("Charging time active. Skipping.")
    return
  }

  runBlocking {
    val propertiesPath = Path(System.getProperty("user.home"), ".reserve-manager")
    if (propertiesPath.notExists()) {
      System.err.println("Warning: $propertiesPath does not exist")
      return@runBlocking
    }
    launch {
      Enphase.fromProperties(propertiesPath, logger) .use { enphase ->
        val result = enphase.setBatteryReserve(reserve)
        logger.info("Setting reserve to $reserve: $result")
      }
    }
  }
}

private fun runTest(idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargingRange: IntRange) {
  val formatter = DateTimeFormatter.ofPattern("HH:mm")
  (0..23).forEach {
    val time = LocalTime.of(it, 0, 0)
    val reserve = calculateReserve(time, idleLoad, batteryCapacity, minReserve, chargingRange)
    val text = if (reserve != null) "$reserve%" else "Skipped"
    println("${time.format(formatter)}: $text")
  }
}

private fun calculateReserve(now: LocalTime, idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargingRange: IntRange): Int? {
  if (now.hour > chargingRange.start && now.hour < chargingRange.endInclusive) {
    return null
  }
  val hours = when (now.hour > chargingRange.start) {
    true -> chargingRange.start + 24 - now.hour
    false -> chargingRange.start - now.hour
  }
  val min = batteryCapacity * minReserve / 100
  val needed = min + idleLoad * hours
  return (needed / batteryCapacity * 100).roundToInt().coerceAtMost(100)
}