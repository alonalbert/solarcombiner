package com.alonalbert.enphase.monitor.ui.energy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.enphase.util.round2
import com.alonalbert.enphase.monitor.repository.Repository
import com.alonalbert.enphase.monitor.util.checkNetwork
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EnergyViewModel @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val repository: Repository,
  db: AppDatabase,
) : ViewModel() {
  private var job: Job? = null

  private val dayFlow = MutableStateFlow(LocalDate.now().atStartOfDay().toLocalDate())

  val dailyEnergyState: StateFlow<DailyEnergy> =
    dayFlow.flatMapLatest { repository.getDailyEnergyFlow(it) }.stateIn(viewModelScope, DailyEnergy.empty(dayFlow.value))
  val batteryStateState: StateFlow<BatteryState> = repository.getBatteryStateFlow().stateIn(viewModelScope, BatteryState(soc = 0, reserve = 0))
  val reserveConfigState: StateFlow<ReserveConfig> =
    db.reserveConfigDao().getReserveConfigFlow().filterNotNull().stateIn(viewModelScope, ReserveConfig())

  private val isRefreshingStateFlow = MutableStateFlow(false)
  val isRefreshing = isRefreshingStateFlow.asStateFlow()

  private val snackbarMessageFlow: MutableStateFlow<String?> = MutableStateFlow(null)
  val snackbarMessageState: StateFlow<String?> = snackbarMessageFlow.stateIn(viewModelScope, null)

  init {
    viewModelScope.launch {
      repository.getTotalsFlow(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 31)).collect { totals ->
        totals.forEach {
          println("${it.day}: Net export: ${(it.export - it.import).round2}")
        }
        val netExport = totals.sumOf { it.export - it.import }.round2
        println("Month: $netExport")
      }
    }
  }

  fun refreshData() {
    Timber.i("refreshData")
    if (!context.checkNetwork()) {
      Timber.w("Network connected but not validated. Might be an issue in Doze. Retrying.")
      return
    }

    job?.cancel()
    job = viewModelScope.launch {
      withRefreshingState {
        try {
          repository.updateRepository(dayFlow.value)
        } catch (e: Throwable) {
          if (e is CancellationException) {
            throw e
          }
          setSnackbarMessage("Error refreshing data")
          Timber.e(e, "repository.updateRepository() error")
        }
      }
    }
  }

  fun setCurrentDay(day: LocalDate) {
    dayFlow.value = day.atStartOfDay().toLocalDate()
    viewModelScope.launch {
      refreshData()
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
