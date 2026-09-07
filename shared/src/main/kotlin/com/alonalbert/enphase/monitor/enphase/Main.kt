package com.alonalbert.enphase.monitor.enphase

import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.inputStream
import kotlin.io.path.notExists

suspend fun main() {
  val homeDir = Path.of(System.getProperty("user.home"))
  val propertiesPath = homeDir.resolve(".reserve-manager")
  if (propertiesPath.notExists()) {
    System.err.println("Warning: $propertiesPath does not exist")
    return
  }
  val properties = Properties()
  propertiesPath.inputStream().use {
    properties.load(it)
  }
  val email = properties.getProperty("login.email") ?: throw IllegalStateException("Missing email")
  val password = properties.getProperty("login.password") ?: throw IllegalStateException("Missing password")
  Enphase({ Credentials(email, password) }).use { enphase ->
    val siteId = properties.getProperty("site.main") ?: throw IllegalStateException("Missing site id")

    enphase.setBatteryReserve(siteId, 20)
//    val response = enphase.getLiveStreamInfo("202507001901")
//    println(response)
  }
}