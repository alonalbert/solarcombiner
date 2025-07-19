package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.calculateEnergyFlow
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
  val enphase = Enphase.fromResourceProperties()

  enphase.streamLiveStatus().collect {
    println(it)
    println(it.calculateEnergyFlow().toString().prependIndent("  "))
  }
}