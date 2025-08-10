package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy

sealed class ChartData {
  class DayData(val dailyEnergy: DailyEnergy): ChartData()
  class MonthData(val days: List<DayTotals>): ChartData()
}
