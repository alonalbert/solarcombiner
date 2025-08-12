package com.alonalbert.enphase.monitor.ui.energy

enum class ProductionSplit {
  EXPORT {
    override fun next() = MAIN
  },
  MAIN {
    override fun next() = NONE
  },
  NONE {
    override fun next() = EXPORT
  },
  ;

  abstract fun next(): ProductionSplit
}
