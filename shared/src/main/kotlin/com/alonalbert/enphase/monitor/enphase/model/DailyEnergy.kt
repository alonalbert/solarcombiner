package com.alonalbert.enphase.monitor.enphase.model

import com.alonalbert.enphase.monitor.enphase.util.kwh

class DailyEnergy(val energies: List<Energy>) {
  val exportProduced = energies.sumOf { it.exportProduced } / 4
  val mainProduced = energies.sumOf { it.mainProduced } / 4
  val consumed = energies.sumOf { it.consumed } / 4
  val mainExported = energies.sumOf { it.mainExported } / 4
  val charged = energies.sumOf { it.charged } / 4
  val discharged = energies.sumOf { it.discharged } / 4
  val produced get() = exportProduced + mainProduced
  val netImported get() = imported - exported
  private val grid = energies.map { (it.imported - it.mainExported - it.exportProduced) / 4 }
  val imported = grid.filter { it > 0 }.sum()
  val exported = -grid.filter { it < 0 }.sum()

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

  companion object {
    val EMPTY = DailyEnergy(List(96) { Energy(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0) })
  }
}