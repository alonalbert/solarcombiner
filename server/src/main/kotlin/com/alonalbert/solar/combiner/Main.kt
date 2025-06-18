package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solarsim.ui.plotEnergy
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
  val dailyEnergy = enphase.getDailyEnergy(LocalDate.of(2025, 6, 17))
  println(dailyEnergy)
  dailyEnergy.plotEnergy("out/test.png")
}