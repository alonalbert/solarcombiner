package com.alonalbert.solar.combiner.enphase.model

class LiveStatus(
  val pv: Double,
  val storage: Double,
  val grid: Double,
  val load: Double,
) {
  override fun toString(): String {
    return buildString {
      append("Producing: ${pv.format()} ")
      if (storage >= 0) {
        append("Discharging: ${storage.format()} ")
      } else {
        append("Charging: ${(-storage).format()} ")
      }
      if (grid >= 0) {
        append("Importing: ${grid.format()} ")
      } else {
        append("Exporting: ${(-grid).format()} ")
      }
      append("Consuming: ${load.format()} ")
    }
  }
}
private fun Double.format() = "%.2f".format(this)
