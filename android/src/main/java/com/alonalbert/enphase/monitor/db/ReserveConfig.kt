package com.alonalbert.enphase.monitor.db

data class ReserveConfig(
  val idleLoad: Double,
  val minReserve: Int,
  val chargeStart: Int,
) {
  companion object {
    val DEFAULT = ReserveConfig(0.8, 20, 9)
  }
}