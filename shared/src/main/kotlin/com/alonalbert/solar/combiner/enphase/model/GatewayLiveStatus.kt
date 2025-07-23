package com.alonalbert.solar.combiner.enphase.model

internal class GatewayLiveStatus(
  val pv: Double,
  val storage: Double,
  val grid: Double,
  val load: Double,
  val soc: Int,
  val reserve: Int,
) {
  override fun toString(): String {
    return "GatewayLiveStatus(pv = $pv, storage = $storage, grid = $grid, load = $load, soc=$soc, reserve=$reserve)"
  }
}
