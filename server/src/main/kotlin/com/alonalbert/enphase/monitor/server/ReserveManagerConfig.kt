package com.alonalbert.enphase.monitor.server

data class ReserveConfig(
  val enabled: Boolean,
  val idleLoad: Double,
  val minReserve: Int,
  val chargeStart: Int,
  val chargeEnd: Int,
)
