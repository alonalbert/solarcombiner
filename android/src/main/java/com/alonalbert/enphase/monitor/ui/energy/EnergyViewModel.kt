package com.alonalbert.enphase.monitor.ui.energy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.Enphase.CacheMode.CACHE_ONLY
import com.alonalbert.enphase.monitor.enphase.Enphase.CacheMode.NO_CACHE
import com.alonalbert.enphase.monitor.enphase.EnphaseException
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.enphase.model.Energy
import com.alonalbert.enphase.monitor.util.checkNetwork
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EnergyViewModel @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val db: AppDatabase,
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private var job: Job? = null
  private var day: LocalDate = LocalDate.now().atStartOfDay().toLocalDate()

  private val dailyEnergyFlow: MutableStateFlow<DailyEnergy> = MutableStateFlow(DailyEnergy.empty(day))
  private val batteryStateFlow: MutableStateFlow<BatteryState> = MutableStateFlow(BatteryState(null, null))

  val dailyEnergyState: StateFlow<DailyEnergy> = dailyEnergyFlow.stateIn(viewModelScope, DailyEnergy.empty(day))
  val batteryStateState: StateFlow<BatteryState> = batteryStateFlow.stateIn(viewModelScope, BatteryState(null, null))

  private val isRefreshingStateFlow = MutableStateFlow(false)
  val isRefreshing = isRefreshingStateFlow.asStateFlow()

  private val snackbarMessageFlow: MutableStateFlow<String?> = MutableStateFlow(null)
  val snackbarMessageState: StateFlow<String?> = snackbarMessageFlow.stateIn(viewModelScope, null)

  private suspend fun enphase(): Enphase {
    val enphase = enphaseAsync.await()
    val settings = settings()
    if (settings != null) {
      try {
        enphase.ensureLogin(settings.email, settings.password)
      } catch (_: Throwable) {
        // Ignore
      }
    }
    return enphase
  }

  private suspend fun settings() = db.settingsDao().getSettings()

  fun refreshData() {
    Timber.i("refreshData")
    if (!context.checkNetwork()) {
      Timber.w("Network connected but not validated. Might be an issue in Doze. Retrying.")
      return
    }

    try {
      job?.cancel()
      job = viewModelScope.launch {
        withRefreshingState {
          try {
            val settings = settings()
            if (settings == null) {
              Timber.w("Settings not found")
              return@withRefreshingState
            }
            val enphase = enphase()
            val deferredBatteryState = async { enphase.getBatteryState(settings.mainSiteId) }
            val deferredDailyEnergy = async { enphase.getDailyEnergy(settings.mainSiteId, settings.exportSiteId, day, NO_CACHE) }
            val dailyEnergy = deferredDailyEnergy.await() ?: return@withRefreshingState
            batteryStateFlow.value = deferredBatteryState.await()
            if (dailyEnergy.date == day) {
              dailyEnergyFlow.value = dailyEnergy
            }
          } catch (e: EnphaseException) {
            Timber.e(e, "Failed to refresh data")
            setSnackbarMessage("Failed to refresh data: ${e.reason}")
          } catch (e: CancellationException) {
            Timber.e(e, "Canceled")
            throw e
          } catch (e: Exception) {
            Timber.e(e, "Failed to refresh data")
            setSnackbarMessage("Failed to refresh data: ${e.message}")
          }
        }
      }
    } catch (e: Exception) {
      Timber.e(e, "Failed to refresh data")
    }
  }

  fun setDay(day: LocalDate, refresh: Boolean = false) {
    this.day = day.atStartOfDay().toLocalDate()
    viewModelScope.launch {
      withRefreshingState {
        val settings = settings()
        if (settings == null) {
          Timber.w("Settings not found")
          return@withRefreshingState
        }
        try {
          val enphase = enphaseAsync.await()
          dailyEnergyFlow.value = enphase.getDailyEnergy(settings.mainSiteId, settings.exportSiteId, day, CACHE_ONLY) ?: DailyEnergy.empty(day)
        } catch (e: EnphaseException) {
          setSnackbarMessage("Error loading energy: ${e.reason}")
          dailyEnergyFlow.value = DailyEnergy(day, List(96) { Energy(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0) })
        }
      }
      if (refresh) {
        refreshData()
      }
    }
  }

  private suspend fun withRefreshingState(block: suspend () -> Unit) {
    isRefreshingStateFlow.value = true
    try {
      block()
    } finally {
      isRefreshingStateFlow.value = false
    }
  }

  fun setSnackbarMessage(message: String) {
    synchronized(snackbarMessageFlow) {
      if (snackbarMessageFlow.value != message) {
        snackbarMessageFlow.value = message
      }
    }
  }

  fun dismissSnackbarMessage() {
    snackbarMessageFlow.value = null
  }
}
