package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.Envoy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Properties

fun main(): Unit = runBlocking {
  val properties = ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use {
    Properties().apply {
      load(it)
    }
  }
  val email = properties.getProperty("login.email")
  val password = properties.getProperty("login.password")
  val mainSerialNum = properties.getProperty("envoy.main.serial")
  val mainHost = properties.getProperty("envoy.main.host")
  val mainPort = properties.getProperty("envoy.main.port").toInt()
  val exportSerialNum = properties.getProperty("envoy.export.serial")
  val exportHost = properties.getProperty("envoy.export.host")
  val exportPort = properties.getProperty("envoy.export.port").toInt()

  val mainEnvoy = Envoy.create(email, password, mainHost, mainPort, mainSerialNum)
  val exportEnvoy = Envoy.create(email, password, exportHost, exportPort, exportSerialNum)

  while (true) {
    val mainData = mainEnvoy.getRealtimeData()
    val exportData = exportEnvoy.getRealtimeData()
    val data = mainData.copy(pv = mainData.pv + exportData.pv, grid = mainData.grid - exportData.pv)
    println(data)
    println("===================")
    delay(1000)
  }
}