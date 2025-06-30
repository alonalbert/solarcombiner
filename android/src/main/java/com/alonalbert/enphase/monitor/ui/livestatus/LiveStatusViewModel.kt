package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.util.stateIn
import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class LiveStatusViewModel @Inject constructor(
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private suspend fun enphase() = enphaseAsync.await()

  val liveStatusFlow: StateFlow<LiveStatus> = flow {
    enphase().streamLiveStatus().collect {
      emit(it)
    }
  }.stateIn(viewModelScope, LiveStatus(0.0, 0.0, 0.0, 0.0))
}