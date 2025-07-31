package com.alonalbert.enphase.monitor.enphase.model

class LiveStatus(
  val pv: Double,
  val exportPv: Double?,
  val storage: Double,
  val grid: Double,
  val load: Double,
  val soc: Int,
  val reserve: Int,
) {
  override fun toString(): String {
    return "LiveStatus(pv = $pv, exportPv=$exportPv, storage = $storage, grid = $grid, load = $load, soc=$soc, reserve=$reserve)"
  }
}
