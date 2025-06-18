package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.Enphase
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
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
  val enphase = Enphase.create(email, password, mainSiteId, exportSiteId)
  val power = enphase.getPower(LocalDate.of(2025, 6, 17))
//  val power = enphase.getPower(LocalDate.now())
  println(power)
}