package com.alonalbert.enphase.monitor.server

internal data class EnphaseConfig(
  val email: String,
  val password: String,
  val mainSite: String,
  val mainSerial: String,
  val mainHost: String,
  val mainPort: Int,
  val exportSite: String,
  val exportSerial: String,
  val exportHost: String,
  val exportPort: Int,
)
