package com.alonalbert.enphase.monitor.server.emporia

import com.alonalbert.enphase.monitor.emporia.Emporia
import com.alonalbert.enphase.monitor.enphase.util.minusMinutes
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MINUTES
import java.util.Properties

suspend fun main() {
  val properties = Properties().apply {
    val classLoader = ClassLoader.getSystemClassLoader()
    classLoader.getResourceAsStream("local.properties").use {
      load(it)
    }
  }

  val username = properties.getProperty("emporia.username")
  val password = properties.getProperty("emporia.password")
  Emporia(username, password).use { emporia ->
//    getUsages(emporia)
    getDailyUsage(emporia, LocalDate.of(2025, 12, 20))
  }
}

private suspend fun getUsages(emporia: Emporia) {
  emporia.getChannels().forEach {
    val end = Instant.now().truncatedTo(MINUTES)
    val start = end.minusMinutes(60 * 12)
    val usage = emporia.getUsage(it, start, end)
    println("${it.name}: ${usage.map { (it * 1000).toInt() }}")
  }
}

private suspend fun getDailyUsage(emporia: Emporia, day: LocalDate) {
  val start = ZonedDateTime.of(day, LocalTime.MIDNIGHT, ZoneId.of("America/Los_Angeles")).toInstant()
  val usage = emporia.getDailyUsage(start)
  usage.forEach { dailyUsage ->
    val hourlyUsage = dailyUsage.usage.toHourlySums()
    val formattedHourlyUsage = hourlyUsage.joinToString(", ") { "%.2f".format(it) }
    println("${dailyUsage.channel.name}: $formattedHourlyUsage")
  }
}

private fun List<Double>.toHourlySums(): List<Double> {
  // Group the list into chunks of 60 minutes (1 hour).
  return this.chunked(60)
    // For each hourly chunk, calculate the sum.
    .map { hourlyMinutes -> hourlyMinutes.sum() }
}
