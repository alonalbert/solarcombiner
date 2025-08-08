package com.alonalbert.enphase.monitor.repository

import android.content.Context
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.Enphase.CacheMode.NO_CACHE
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
        val energies = enphase.getDailyEnergy(settings.mainSiteId, settings.exportSiteId, day, NO_CACHE)?.energies ?: return@launch
        val produced = energies.map { it.mainProduced }
        val consumed = energies.map { it.consumed }
        val charged = energies.map { it.charged }
        val discharged = energies.map { it.discharged }
        val imported = energies.map { it.imported }
        val exported = energies.map { it.mainExported }
        val battery = energies.map { it.battery }
        db.dayDao().updateDay(
          day,
          produced,
          consumed,
          charged,
          discharged,
          imported,
          exported,
          battery,
          )
      }
    }
  }
}