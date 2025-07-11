package com.alonalbert.enphase.monitor.ui.energy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EnergyViewModel @Inject constructor(
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private var job: Job? = null
  private var day: LocalDate = LocalDate.now().atStartOfDay().toLocalDate()

  private val dailyEnergyFlow: MutableStateFlow<DailyEnergy?> = MutableStateFlow(null)
  private val batteryStateFlow: MutableStateFlow<BatteryState> = MutableStateFlow(BatteryState(null, null))

  val dailyEnergyState: StateFlow<DailyEnergy?> = dailyEnergyFlow.stateIn(viewModelScope, null)
  val batteryStateState: StateFlow<BatteryState> = batteryStateFlow.stateIn(viewModelScope, BatteryState(null, null))

  private suspend fun enphase() = enphaseAsync.await()

  fun refreshData() {
    viewModelScope.launch {
      batteryStateFlow.value = enphase().getBatteryState()
    }
    job?.cancel()
    job = viewModelScope.launch {
      val dailyEnergy = enphase().getDailyEnergy(day, NO_CACHE) ?: return@launch
      if (dailyEnergy.date == day) {
        dailyEnergyFlow.value = dailyEnergy
      }
    }
  }

  fun setDay(day: LocalDate) {
    this.day = day.atStartOfDay().toLocalDate()
    viewModelScope.launch {
      dailyEnergyFlow.value = enphase().getDailyEnergy(day, CACHE_ONLY)
    }
    refreshData()
  }
}
