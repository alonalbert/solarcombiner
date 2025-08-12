package com.alonalbert.enphase.monitor.ui.energy

enum class ProductionSplit {
  EXPORT {
    override fun not() = MAIN
  },
  MAIN {
    override fun not() = EXPORT
  },
  ;

  abstract operator fun not(): ProductionSplit
}
