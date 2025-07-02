package com.alonalbert.solar.combiner.enphase.model

import com.alonalbert.solar.combiner.enphase.util.kwh
import java.time.LocalDate

class DailyEnergy(val date: LocalDate, val energies: List<Energy>) {
  val outerProduced = energies.sumOf { it.outerProduced } / 4
  val innerProduced = energies.sumOf { it.innerProduced } / 4
  val consumed = energies.sumOf { it.consumed } / 4
  val imported = energies.sumOf { it.imported } /4
  val innerExported = energies.sumOf { it.innerExported } / 4
  val charged = energies.sumOf { it.charged } / 4
  val discharged = energies.sumOf { it.discharged } / 4
  val produced get() = outerProduced + innerProduced
  val exported get() = innerExported + outerProduced
  val netImported get() = imported - exported

  override fun toString(): String {
    return buildString {
      appendLine("Imported: ${imported.kwh}")
      appendLine("Produced: ${innerProduced.kwh} / ${outerProduced.kwh}")
      appendLine("Discharged: ${discharged.kwh}")
      appendLine("Consumed: ${consumed.kwh}")
      appendLine("Exported: ${innerExported.kwh}")
      appendLine("Charged: ${charged.kwh}")
      val balance = imported + innerProduced + discharged - consumed - innerExported - charged
      appendLine("Balance: $balance")
    }
  }
}