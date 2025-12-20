package com.alonalbert.enphase.monitor.server

import com.alonalbert.enphase.monitor.emporia.Emporia
import java.time.Instant
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
    emporia.getChannels().forEach {
      val end = Instant.now().truncatedTo(MINUTES)
      val start = end.minusSeconds(60)
      val usage = emporia.getUsage(it, start, end)
      println("${it.name}: ${usage.map { (it * 1000).toInt() }}")
    }
  }
}