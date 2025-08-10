package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.ui.datepicker.DayPeriod
import java.time.LocalDate

class DayData(
  day: LocalDate,
  val productionMain: List<Double>,
  val productionExport: List<Double>,
  val consumption: List<Double>,
  val charge: List<Double>,
  val discharge: List<Double>,
  val import: List<Double>,
  val export: List<Double>,
  val battery: List<Int?>,
) : ChartData(DayPeriod(day)) {

  val production = (0..95).map { (productionMain[it] + productionExport[it]) }
  val grid = (0..95).map { (import[it] - export[it] - productionExport[it]) }
  val storage = (0..95).map { (discharge[it] - charge[it]) }

  val totalProductionMain = productionMain.sum()
  val totalProductionExport = productionExport.sum()
  val totalConsumption = consumption.sum()
  val totalCharge = charge.sum()
  val totalDischarge = discharge.sum()
  val totalImport = grid.filter { it > 0 }.sum()
  val totalExport = -grid.filter { it < 0 }.sum()

  companion object {
    fun empty(day: LocalDate) = DayData(
      day,
      List(96) { 0.0 },
      List(96) { 0.0 },
      List(96) { 0.0 },
      List(96) { 0.0 },
      List(96) { 0.0 },
      List(96) { 0.0 },
      List(96) { 0.0 },
      List(96) { 0 },
    )
  }
}