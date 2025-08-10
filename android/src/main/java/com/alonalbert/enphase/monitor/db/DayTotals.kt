package com.alonalbert.enphase.monitor.db

import java.time.LocalDate

data class DayTotals(
  val day: LocalDate,
  val production: Double,
  val consumption: Double,
  val charge: Double,
  val discharge: Double,
  val import: Double,
  val export: Double,
  val exportProduction: Double,
) {
  override fun toString(): String {
    return """DayTotals(day="$day", production=$production, exportProduction=$exportProduction, consumption=$consumption, charge=$charge, discharge=$discharge, import=$import, export=$export)"""
  }

  companion object {
    fun empty(day: LocalDate) = DayTotals(day, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
  }
}