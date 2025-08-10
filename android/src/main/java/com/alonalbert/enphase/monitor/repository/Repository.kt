package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.BatteryStatus
import com.alonalbert.enphase.monitor.db.DayExportValues
import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.db.DayValues
import com.alonalbert.enphase.monitor.db.DayWithExportValues
import com.alonalbert.enphase.monitor.db.DayWithValues
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.ui.datepicker.DayPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.Period
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    val dao = db.dayDao()
    val valuesFlow = dao.getDayWithValuesFlow(day)
    val exportValuesFlow = dao.getDayWithExportValuesFlow(day)
    return valuesFlow.combine(exportValuesFlow) { values, exportValues ->
      val values = values.valuesOrEmpty()
      DayData(
        day = day,
        productionMain = exportValues.valuesOrEmpty().map { it.production / 1000 },
        productionExport = values.map { it.production / 1000 },
        consumption = values.map { it.consumption / 1000 },
        charge = values.map { it.charge / 1000 },
        discharge = values.map { it.discharge / 1000 },
        import = values.map { it.import / 1000 },
        export = values.map { it.export / 1000 },
        battery = values.map { it.battery },
      )
    }
  }

  private fun getMonthDataFlow(month: YearMonth): Flow<ChartData> {
    val start = month.atDay(1)
    val end = month.atEndOfMonth()

    return db.dayDao().getTotalsFlow(start, end).map { days ->
      val dayMap = days.associateBy { it.day.dayOfMonth }
      val allDays = (1..month.lengthOfMonth()).map {
        dayMap[it] ?: DayTotals.empty(month.atDay(it))
      }
      MonthData(month, allDays)
    }
  }
}

private fun DayWithValues?.valuesOrEmpty(): List<DayValues> {
  return when {
    this == null || values.isEmpty() -> List(96) { DayValues(0, 0, it, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0) }
    else -> values
  }
}

private fun DayWithExportValues?.valuesOrEmpty(): List<DayExportValues> {
  return when {
    this == null || values.isEmpty() -> List(96) { DayExportValues(0, 0, it, 0.0) }
    else -> values
  }
}
