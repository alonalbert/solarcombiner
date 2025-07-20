package com.alonalbert.enphase.monitor.ui.livestatus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.settings.getSettings
import com.alonalbert.enphase.monitor.util.stateIn
import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LiveStatusViewModel @Inject constructor(
  private val application: TheApplication,
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private suspend fun enphase() = enphaseAsync.await()
  private suspend fun settings() = application.getSettings()

  val liveStatusFlow: StateFlow<LiveStatus> = flow {
    val settings = settings()
    enphase().streamLiveStatus(settings.email, settings.mainGatewayConfig, settings.exportGatewayConfig).collect {
      Timber.log(Log.DEBUG, "$it")
      emit(it)
    }
  }.stateIn(viewModelScope, LiveStatus(0.0, 0.0, 0.0, 0.0, 0, 0))
}