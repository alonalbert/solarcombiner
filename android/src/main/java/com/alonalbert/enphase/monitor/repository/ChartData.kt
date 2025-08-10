package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import java.time.LocalDate
import java.time.YearMonth

sealed class ChartData {
  class DayData(val day: LocalDate, val dailyEnergy: DailyEnergy): ChartData()
  class MonthData(val month: YearMonth, val days: List<DayTotals>): ChartData()
}
