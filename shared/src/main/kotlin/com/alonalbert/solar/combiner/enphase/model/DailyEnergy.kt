package com.alonalbert.solar.combiner.enphase.model

import com.alonalbert.solar.combiner.enphase.util.kwh
import java.time.LocalDate

class DailyEnergy(val date: LocalDate, val energies: List<Energy>) {
  val exportProduced = energies.sumOf { it.exportProduced } / 4
  val mainProduced = energies.sumOf { it.mainProduced } / 4
  val consumed = energies.sumOf { it.consumed } / 4
  val imported = energies.sumOf { it.imported } /4
  val mainExported = energies.sumOf { it.mainExported } / 4
  val charged = energies.sumOf { it.charged } / 4
  val discharged = energies.sumOf { it.discharged } / 4
  val produced get() = exportProduced + mainProduced
  val exported get() = mainExported + exportProduced
  val netImported get() = imported - exported

  override fun toString(): String {
    return buildString {
      appendLine("Imported: ${imported.kwh}")
      appendLine("Produced: ${mainProduced.kwh} / ${exportProduced.kwh}")
      appendLine("Discharged: ${discharged.kwh}")
      appendLine("Consumed: ${consumed.kwh}")
      appendLine("Exported: ${mainExported.kwh}")
      appendLine("Charged: ${charged.kwh}")
      val balance = imported + mainProduced + discharged - consumed - mainExported - charged
      appendLine("Balance: $balance")
    }
  }
}