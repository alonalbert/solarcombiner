package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.BatteryStatus
import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.repository.ChartData.DayData
import com.alonalbert.enphase.monitor.repository.ChartData.MonthData
import com.alonalbert.enphase.monitor.ui.energy.Period
import com.alonalbert.enphase.monitor.ui.energy.Period.DayPeriod
import com.alonalbert.enphase.monitor.ui.energy.Period.MonthPeriod
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class Repository @Inject constructor(
  val db: AppDatabase,
) {
  private val enphase: Enphase = Enphase()

  fun getChartDataFlow(period: Period): Flow<ChartData> {
    return when (period) {
      is DayPeriod -> getDayDataFlow(period.day)
      is MonthPeriod -> getMonthDataFlow(period.month)
    }
  }

  fun getBatteryStateFlow(): Flow<BatteryState> =
    db.batteryStatusDao()
      .getBatteryStatusFlow()
      .filterNotNull()
      .map { BatteryState(it.battery, it.reserve) }

  suspend fun updateRepository(day: LocalDate) {
    val settings = db.settingsDao().getSettings() ?: return
    enphase.ensureLogin(settings.email, settings.password)
    coroutineScope {
      launch {
        val stats = enphase.getMainStats(settings.mainSiteId, day)
        db.dayDao().updateValues(
          day,
          stats.production,
          stats.consumption,
          stats.charge,
          stats.discharge,
          stats.import,
          stats.export,
          stats.battery,
        )
      }

      launch {
        val stats = enphase.getExportStats(settings.exportSiteId, day)
        db.dayDao().updateExportValues(
          day,
          stats.production,
        )
      }

      launch {
        val batteryState = enphase.getBatteryState(settings.mainSiteId)
        db.batteryStatusDao().set(
          BatteryStatus(
            battery = batteryState.soc ?: 0,
            reserve = batteryState.reserve ?: 0
          )
        )
      }
    }
  }

  private fun getDayDataFlow(day: LocalDate): Flow<ChartData> {
    return db.dayDao().getDailyEnergyFlow(day).map { DayData(day, it) }
  }

  private fun getMonthDataFlow(month: YearMonth): Flow<ChartData> {
    val start = month.atDay(1)
    val end = month.atEndOfMonth()

    return db.dayDao().getTotalsFlow(start, end).map { days ->
      val size = days.size
      val emptyDays = month.lengthOfMonth() - size
      val allDays = days + List(emptyDays) { DayTotals.empty(month.atDay(it + size + 1)) }
      MonthData(YearMonth.now(), allDays)
    }
  }
}