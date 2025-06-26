package com.alonalbert.enphase.monitor.ui.energy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.util.stateIn
import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solarsim.simulator.DailyEnergy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EnergyViewModel @Inject constructor(
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private suspend fun enphase() = enphaseAsync.await()

  private val dailyEnergyFlow: MutableStateFlow<DailyEnergy?> = MutableStateFlow(null)
  val dailyEnergyState: StateFlow<DailyEnergy?> = dailyEnergyFlow.stateIn(viewModelScope, null)

  fun setDay(day: LocalDate) {
    viewModelScope.launch {
      dailyEnergyFlow.value = enphase().getDailyEnergy(day)
    }
  }
}