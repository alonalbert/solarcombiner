package com.alonalbert.enphase.monitor.repository

import android.content.Context
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class Repository @Inject constructor(
  @param:ApplicationContext private val context: Context,
  val db: AppDatabase,
) {
  private val enphase: Enphase = Enphase(context.cacheDir.toPath())

  fun getDailyEnergyFlow(day: LocalDate): Flow<DailyEnergy> {
    return db.dayDao().getDailyEnergyFlow(day)
  }

  suspend fun updateDailyEnergy(day: LocalDate) {
    val settings = db.settingsDao().getSettings() ?: return
    enphase.ensureLogin(settings.email, settings.password)
    coroutineScope {
      launch {
        val mainStats = enphase.getMainStats(settings.mainSiteId, day)
        db.dayDao().updateDay(
          day,
          mainStats.production,
          mainStats.consumption,
          mainStats.charge,
          mainStats.discharge,
          mainStats.import,
          mainStats.export,
          mainStats.battery,
        )
      }
    }
  }
}