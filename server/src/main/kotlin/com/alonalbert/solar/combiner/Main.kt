package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.Enphase
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.util.Properties

fun main() = runBlocking {
  val properties = ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use {
    Properties().apply {
      load(it)
    }
  }
  val email = properties.getProperty("login.email")
  val password = properties.getProperty("login.password")
  val mainSiteId = properties.getProperty("site.main")
  val exportSiteId = properties.getProperty("site.export")
  val mainSerialNum = properties.getProperty("envoy.main.serial")
  val mainHost = properties.getProperty("envoy.main.host")
  val mainPort = properties.getProperty("envoy.main.port").toInt()
  val exportSerialNum = properties.getProperty("envoy.export.serial")
  val exportHost = properties.getProperty("envoy.export.host")
  val exportPort = properties.getProperty("envoy.export.port").toInt()
  val enphase = Enphase(
    email,
    password,
    mainSiteId,
    mainSerialNum,
    mainHost,
    mainPort,
    exportSiteId,
    exportSerialNum,
    exportHost,
    exportPort,
    Path.of("cache"),
    this,
  )
  enphase.streamLiveStatus().collect {
    println(it)
    println("============================")
  }
}