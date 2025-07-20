package com.alonalbert.enphase.monitor.ui.energy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.exportGateway
import com.alonalbert.enphase.monitor.db.mainGateway
import com.alonalbert.enphase.monitor.util.stateIn
import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.CACHE_ONLY
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.NO_CACHE
import com.alonalbert.solar.combiner.enphase.model.BatteryState
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EnergyViewModel @Inject constructor(
  private val db: AppDatabase,
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private var job: Job? = null
  private var day: LocalDate = LocalDate.now().atStartOfDay().toLocalDate()

  private val dailyEnergyFlow: MutableStateFlow<DailyEnergy?> = MutableStateFlow(null)
  private val batteryStateFlow: MutableStateFlow<BatteryState> = MutableStateFlow(BatteryState(null, null))

  val dailyEnergyState: StateFlow<DailyEnergy?> = dailyEnergyFlow.stateIn(viewModelScope, null)
  val batteryStateState: StateFlow<BatteryState> = batteryStateFlow.stateIn(viewModelScope, BatteryState(null, null))

  private val isRefreshingStateFlow = MutableStateFlow(false)
  val isRefreshing = isRefreshingStateFlow.asStateFlow()

  private suspend fun enphase(): Enphase {
    val enphase = enphaseAsync.await()
    val settings = settings()
    enphase.login(settings.email, settings.password)
    return enphase
  }

  private suspend fun settings() = db.settingsDao().getSettings()
  private suspend fun mainSiteId() = settings().mainGateway.siteId
  private suspend fun exportSiteId() = settings().exportGateway?.siteId

  fun refreshData() {
    viewModelScope.launch {
      refreshData {
        batteryStateFlow.value = enphase().getBatteryState(mainSiteId())
      }
    }
    job?.cancel()
    job = viewModelScope.launch {
      refreshData {
        val dailyEnergy = enphase().getDailyEnergy(mainSiteId(), exportSiteId(), day, NO_CACHE) ?: return@refreshData
        if (dailyEnergy.date == day) {
          dailyEnergyFlow.value = dailyEnergy
        }
      }
    }
  }

  fun setDay(day: LocalDate) {
    this.day = day.atStartOfDay().toLocalDate()
    viewModelScope.launch {
      dailyEnergyFlow.value = enphase().getDailyEnergy(mainSiteId(), exportSiteId(), day, CACHE_ONLY)
    }
    refreshData()
  }

  private suspend fun refreshData(block: suspend () -> Unit) {
    isRefreshingStateFlow.value = true
    try {
      block()
    } finally {
      isRefreshingStateFlow.value = false
    }
  }
}
