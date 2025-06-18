package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.Enphase
import kotlinx.coroutines.runBlocking
import java.util.Properties

fun main() = runBlocking {
  val properties = ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use {
    Properties().apply {
      load(it)
    }
  }
  val email = properties.getProperty("login.email")
  val password = properties.getProperty("login.password")
  val enphase = Enphase.create(email, password)
}