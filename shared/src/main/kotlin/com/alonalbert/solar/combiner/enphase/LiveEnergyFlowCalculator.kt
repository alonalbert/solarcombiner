package com.alonalbert.solar.combiner.enphase

import com.alonalbert.solar.combiner.enphase.model.LiveEnergyFlow
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import com.alonalbert.solar.combiner.enphase.util.zerofy
import kotlin.math.min

fun LiveStatus.calculateEnergyFlow(): LiveEnergyFlow {
  var pvToLoad = 0.0
  var pvToStorage = 0.0
  var pvToGrid = 0.0
  var gridToLoad = 0.0
  var gridToStorage = 0.0
  var storageToLoad = 0.0
  var storageToGrid = 0.0
  var pv = this.pv
  var load = this.load
  var storage = this.storage
  var grid = this.grid

  if (pv.zerofy() > 0) {
    if (grid.zerofy() < 0) {
      val e = min(pv, -grid)
      pv -= e
      grid += e
      pvToGrid += e
    }
  }
  if (pv > 0) {
    if (storage < 0) {
      val e = min(pv, -storage)
      pv -= e
      storage += e
      pvToStorage += e
    }
  }
  if (pv.zerofy() > 0) {
    if (load.zerofy() > 0) {
      val e = min(pv, load)
      pv -= e
      load -= e
      pvToLoad += e
    }
  }

  if (storage.zerofy() > 0) {
    if (load.zerofy() > 0) {
      val e = min(storage, load)
      storage -= e
      load -= e
      storageToLoad += e
    }
  }
  if (storage.zerofy() > 0) {
    if (grid.zerofy() < 0) {
      val e = min(storage, -grid)
      storage -= e
      grid += e
      storageToGrid += e
    }
  }

  if (grid.zerofy() > 0) {
    if (load.zerofy() > 0) {
      val e = min(grid, load)
      grid -= e
      load -= e
      gridToLoad += e
    }
  }
  if (grid.zerofy() > 0) {
    if (storage.zerofy() < 0) {
      val e = min(grid, -storage)
      grid -= e
      storage += e
      gridToStorage += e
    }
  }
  if (pv.zerofy() != 0.0 || storage.zerofy() != 0.0 || grid.zerofy() != 0.0 || load.zerofy() != 0.0 ) {
    throw IllegalArgumentException("Unexpected LiveStatus: $this")
  }

  return LiveEnergyFlow(
    pvToLoad.zerofy(),
    pvToStorage.zerofy(),
    pvToGrid.zerofy(),
    gridToLoad.zerofy(),
    gridToStorage.zerofy(),
    storageToLoad.zerofy(),
    storageToGrid.zerofy(),
  )
}

fun main() {
  val liveStatus = LiveStatus(pv = 9.808691, storage = -2.238, grid = -8.6881, load = -1.117409, soc=36, reserve=20)
  println(liveStatus)
  println(liveStatus.calculateEnergyFlow())
}