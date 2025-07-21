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
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Properties
import kotlin.io.path.inputStream
import kotlin.io.path.notExists
import kotlin.math.roundToInt

private val logger = DefaultLogger()

fun main(args: Array<String>) {
  val parser = ArgParser("reserve-manager")

  val idleLoad by parser.option(ArgType.Double, shortName = "i", fullName = "idle-load", description = "Idle consumption (kWh)").required()
  val batteryCapacity by parser.option(ArgType.Double, shortName = "b", fullName = "battery-capacity", description = "Battery capacity (kWh)")
    .required()
  val minReserve by parser.option(ArgType.Int, shortName = "r", fullName = "min-reserve", description = "Min reserve %").default(20)
  val chargeStart by parser.option(ArgType.Int, shortName = "s", fullName = "charge-start", description = "Hour at which solar charging starts")
    .default(9)
  val testMode by parser.option(ArgType.Boolean, shortName = "t", fullName = "test-mode", description = "Print out list of actions").default(false)

  parser.parse(args)


  when (testMode) {
    true -> runTest(idleLoad, batteryCapacity, minReserve, chargeStart)
    false -> setReserve(idleLoad, batteryCapacity, minReserve, chargeStart)
  }
}

private fun setReserve(idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargeStart: Int) {
  val now = LocalTime.now(ZoneId.systemDefault())
  val reserve = calculateReserve(now, idleLoad, batteryCapacity, minReserve, chargeStart)

  runBlocking {
    val homeDir = Path.of(System.getProperty("user.home"))
    val propertiesPath = homeDir.resolve(".reserve-manager")
    if (propertiesPath.notExists()) {
      System.err.println("Warning: $propertiesPath does not exist")
      return@runBlocking
    }
    launch {
      Enphase(homeDir.resolve(".enphase-cache"), logger).use { enphase ->
        val properties = Properties()
        propertiesPath.inputStream().use {
          properties.load(it)
        }
        val email = properties.getProperty("login.email") ?: throw IllegalStateException("Missing email")
        val password = properties.getProperty("login.password") ?: throw IllegalStateException("Missing password")
        val siteId = properties.getProperty("site.main") ?: throw IllegalStateException("Missing site id")
        enphase.login(email, password)
        val result = enphase.setBatteryReserve(siteId, reserve)
        logger.info("Setting reserve to $reserve: $result")
      }
    }
  }
}

private fun runTest(idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargeStart: Int) {
  val formatter = DateTimeFormatter.ofPattern("HH:mm")
  (0..23).forEach { h ->
    sequenceOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55).forEach { m ->
      val time = LocalTime.of(h, m, 0)
      val reserve = calculateReserve(time, idleLoad, batteryCapacity, minReserve, chargeStart)
      val previousReserve = calculateReserve(time.minusMinutes(5), idleLoad, batteryCapacity, minReserve, chargeStart)
      if (reserve != previousReserve) {
        println("${time.format(formatter)}: $reserve%")
      }
    }
  }
}

const val YEAR = 2000
const val MONTH = 1
const val DAY = 2
private fun calculateReserve(now: LocalTime, idleLoad: Double, batteryCapacity: Double, minReserve: Int, chargeStart: Int): Int {
  val day = if (now.hour < chargeStart) DAY else DAY - 1
  val nowDateTime = LocalDateTime.of(YEAR, MONTH, day, now.hour, now.minute)
  val chargeStartDateTime = LocalDateTime.of(YEAR, MONTH, DAY, chargeStart, 0)
  val duration = Duration.between(nowDateTime, chargeStartDateTime)
  val minutes = duration.toMinutes()
  val min = batteryCapacity * minReserve / 100
  val needed = min + idleLoad * (minutes.toDouble() / 60)
  return (needed / batteryCapacity * 100).roundToInt().coerceAtMost(100)
}