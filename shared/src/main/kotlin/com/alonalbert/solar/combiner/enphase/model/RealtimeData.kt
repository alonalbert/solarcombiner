package com.alonalbert.solar.combiner.enphase.model

data class RealtimeData(
  val pv: Double,
  val storage: Double,
  val grid: Double,
  val load: Double,
) {
  override fun toString(): String {
    val format = "%-10s %+1.2f kW\n"
    return buildString {
      append(format.format("pv:", pv))
      append(format.format("storage:", storage))
      append(format.format("grid:", grid))
      append(format.format("load:", load))
    }
  }
}
