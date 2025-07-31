package com.alonalbert.enphase.monitor.enphase.model

data class Energy(
  val exportProduced: Double,
  val mainProduced: Double,
  val consumed: Double,
  val charged: Double,
  val discharged: Double,
  val mainExported: Double,
  val imported: Double,
  val battery: Int?,
)