package com.alonalbert.solar.combiner.enphase.model

import com.alonalbert.solar.combiner.enphase.util.kwh

data class Power(
  val outerProduction: List<Double>,
  val innerProduction: List<Double>,
  val consumption: List<Double>,
  val charge: List<Double>,
  val discharge: List<Double>,
  val innerExport: List<Double>,
  val import: List<Double>,
) {
  override fun toString(): String {
    val imported = import.sum()
    val innerProduced = innerProduction.sum()
    val outerProduced = outerProduction.sum()
    val discharged = discharge.sum()
    val consumed = consumption.sum()
    val exported = innerExport.sum()
    val charged = charge.sum()

    return buildString {
      appendLine("Imported: ${imported.kwh}")
      appendLine("Produced: ${innerProduced.kwh} / ${outerProduced.kwh}")
      appendLine("Discharged: ${discharged.kwh}")
      appendLine("Consumed: ${consumed.kwh}")
      appendLine("Exported: ${exported.kwh}")
      appendLine("Charged: ${charged.kwh}")
      val balance = imported + innerProduced + discharged - consumed - exported - charged
      appendLine("Balance: $balance")
    }
  }
}
