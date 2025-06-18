package com.alonalbert.solarsim.simulator

import com.alonalbert.solar.combiner.enphase.util.kwh
import java.time.LocalDate

class DailyEnergy(val date: LocalDate, val energies: List<Energy>) {
  val outerProduced = energies.sumOf { it.outerProduced }
  val innerProduced = energies.sumOf { it.innerProduced }
  val consumed = energies.sumOf { it.consumed }
  val imported = energies.sumOf { it.imported }
  val innerExported = energies.sumOf { it.innerExported }
  val charged = energies.sumOf { it.charged }
  val discharged = energies.sumOf { it.discharged }
  val battery = energies.sumOf { it.battery }

  override fun toString(): String {
    return buildString {
      appendLine("Imported: ${imported.kwh}")
      appendLine("Produced: ${innerProduced.kwh} / ${outerProduced.kwh}")
      appendLine("Discharged: ${discharged.kwh}")
      appendLine("Consumed: ${consumed.kwh}")
      appendLine("Exported: ${innerExported.kwh}")
      appendLine("Charged: ${charged.kwh}")
      appendLine("Battery: ${battery.kwh}")
      val balance = imported + innerProduced + discharged - consumed - innerExported - charged
      appendLine("Balance: $balance")
    }
  }
}