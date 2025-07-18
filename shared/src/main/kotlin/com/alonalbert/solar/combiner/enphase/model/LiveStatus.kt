package com.alonalbert.solar.combiner.enphase.model

class LiveStatus(
  val pv: Double,
  val storage: Double,
  val grid: Double,
  val load: Double,
  val soc: Int,
  val reserve: Int,
) {
  override fun toString(): String {
    return "LiveStatus(pv = $pv, storage = $storage, grid = $grid, load = $load, soc=$soc, reserve=$reserve)"
  }
}
