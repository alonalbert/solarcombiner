package com.alonalbert.enphase.monitor.ui.energy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.repository.ChartData
import com.alonalbert.enphase.monitor.repository.DayData
import com.alonalbert.enphase.monitor.repository.Repository
import com.alonalbert.enphase.monitor.ui.datepicker.DayPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.Period
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

  private val periodFlow: MutableStateFlow<Period> = MutableStateFlow(DayPeriod(LocalDate.now().atStartOfDay().toLocalDate()))

  val chartDataFlow: StateFlow<ChartData> =
    periodFlow.flatMapLatest {
      repository.getChartDataFlow(it)
    }.stateIn(viewModelScope, DayData.empty(LocalDate.now()))

  val batteryStateState: StateFlow<BatteryState> = repository.getBatteryStateFlow().stateIn(viewModelScope, BatteryState(soc = 0, reserve = 0))
  val reserveConfigState: StateFlow<ReserveConfig> =
    db.reserveConfigDao().getReserveConfigFlow().filterNotNull().stateIn(viewModelScope, ReserveConfig())

  private val isRefreshingStateFlow = MutableStateFlow(false)
  val isRefreshing = isRefreshingStateFlow.asStateFlow()

  private val snackbarMessageFlow: MutableStateFlow<String?> = MutableStateFlow(null)
  val snackbarMessageState: StateFlow<String?> = snackbarMessageFlow.stateIn(viewModelScope, null)

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
          val period = periodFlow.value
          when (period) {
            is DayPeriod -> repository.updateRepository(period.day)
            is MonthPeriod -> repository.updateRepository(Period.today().day) // TODO(): Update month
          }
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

  fun setPeriod(period: Period) {
    periodFlow.value = period
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
