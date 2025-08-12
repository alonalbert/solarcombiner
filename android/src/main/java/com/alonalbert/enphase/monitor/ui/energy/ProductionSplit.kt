package com.alonalbert.enphase.monitor.ui.energy

enum class ProductionSplit {
  MAIN {
    override fun not() = EXPORT
  },
  EXPORT {
    override fun not() = MAIN
  }
  ;

  abstract operator fun not(): ProductionSplit
}
