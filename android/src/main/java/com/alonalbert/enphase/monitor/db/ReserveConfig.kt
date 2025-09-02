package com.alonalbert.enphase.monitor.db

data class ReserveConfig(
  val enabled: Boolean,
  val idleLoad: Double,
  val minReserve: Int,
  val chargeStart: Int,
  val chargeEnd: Int,
) {
  companion object {
    val DEFAULT = ReserveConfig(false, 0.8, 20, 9, 14)
  }
}