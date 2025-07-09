package com.alonalbert.enphase.monitor.ui.energy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.util.stateIn
import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.CACHE_ONLY
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.NO_CACHE
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

  private val dailyEnergyFlow: MutableStateFlow<DailyEnergy?> = MutableStateFlow(null)

  val dailyEnergyState: StateFlow<DailyEnergy?> = dailyEnergyFlow.stateIn(viewModelScope, null)

  private suspend fun enphase() = enphaseAsync.await()

  fun setDay(day: LocalDate) {
    job?.cancel()
    job = viewModelScope.launch {
      dailyEnergyFlow.value = enphase().getDailyEnergy(day, CACHE_ONLY)
      dailyEnergyFlow.value = enphase().getDailyEnergy(day, NO_CACHE)    }
  }
}
