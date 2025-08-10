package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.BatteryStatus
import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.enphase.util.format
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class Repository @Inject constructor(
  val db: AppDatabase,
) {
  private val enphase: Enphase = Enphase()

  fun getDailyEnergyFlow(day: LocalDate): Flow<DailyEnergy> = db.dayDao().getDailyEnergyFlow(day)

  fun getTotalsFlow(start: LocalDate, end: LocalDate): Flow<List<DayTotals>> =
    db.dayDao().getTotalsFlow(start.format(), end.format())

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
}