package com.alonalbert.enphase.monitor.enphase.model

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
      if (pvToLoad > 0) {
        appendLine(format.format("pvToLoad", pvToLoad))
      }
      if (pvToStorage > 0) {
        appendLine(format.format("pvToStorage", pvToStorage))
      }
      if (pvToGrid > 0) {
        appendLine(format.format("pvToGrid", pvToGrid))
      }
      if (storageToLoad > 0) {
        appendLine(format.format("storageToLoad", storageToLoad))
      }
      if (storageToGrid > 0) {
        appendLine(format.format("storageToGrid", storageToGrid))
      }
      if (gridToLoad > 0) {
        appendLine(format.format("gridToLoad", gridToLoad))
      }
      if (gridToStorage > 0) {
        appendLine(format.format("gridToStorage", gridToStorage))
      }
    }
  }
}
