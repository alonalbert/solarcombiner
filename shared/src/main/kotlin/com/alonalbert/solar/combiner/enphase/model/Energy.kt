package com.alonalbert.solarsim.simulator

data class Energy(
  val outerProduced: Double,
  val innerProduced: Double,
  val consumed: Double,
  val charged: Double,
  val discharged: Double,
  val innerExported: Double,
  val imported: Double,
  val battery: Int?,
)