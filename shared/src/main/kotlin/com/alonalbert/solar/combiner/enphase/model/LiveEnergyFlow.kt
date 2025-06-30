package com.alonalbert.solar.combiner.enphase.model

import com.alonalbert.solar.combiner.enphase.util.zerofy

private const val format = "%-12s: %.2f"

class LiveEnergyFlow(
  val pvToLoad: Double,
  val pvToStorage: Double,
  val pvToGrid: Double,
  val gridToLoad: Double,
  val gridToStorage: Double,
  val storageToLoad: Double,
  val storageToGrid: Double,
) {
  override fun toString(): String {
    return buildString {
      if (pvToLoad.zerofy() > 0) {
        appendLine(format.format("pvToLoad", pvToLoad))
      }
      if (pvToStorage.zerofy() > 0) {
        appendLine(format.format("pvToStorage", pvToStorage))
      }
      if (pvToGrid.zerofy() > 0) {
        appendLine(format.format("pvToGrid", pvToGrid))
      }
      if (storageToLoad.zerofy() > 0) {
        appendLine(format.format("storageToLoad", storageToLoad))
      }
      if (storageToGrid.zerofy() > 0) {
        appendLine(format.format("storageToGrid", storageToGrid))
      }
      if (gridToLoad.zerofy() > 0) {
        appendLine(format.format("gridToLoad", gridToLoad))
      }
      if (gridToStorage.zerofy() > 0) {
        appendLine(format.format("gridToStorage", gridToStorage))
      }
    }
  }
}
