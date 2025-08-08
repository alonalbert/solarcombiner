package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class Repository @Inject constructor(
  val db: AppDatabase,
) {
  private val enphase: Enphase = Enphase()

  fun getDailyEnergyFlow(day: LocalDate): Flow<DailyEnergy> {
    return db.dayDao().getDailyEnergyFlow(day)
  }

  suspend fun updateDailyEnergy(day: LocalDate) {
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
    }
  }
}