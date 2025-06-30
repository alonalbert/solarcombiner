package com.alonalbert.solar.combiner.enphase.model

class LiveStatus(
  val pv: Double,
  val storage: Double,
  val grid: Double,
  val load: Double,
) {
  override fun toString(): String {
    return "LiveStatus(pv = %f, storage = %f, grid = %f, load = %f)".format(pv, storage, grid, load)
  }
}
