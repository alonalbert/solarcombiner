package com.alonalbert.enphase.monitor.ui.livestatus

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.exportGateway
import com.alonalbert.enphase.monitor.db.mainGateway
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
  private val db: AppDatabase,
  private val enphaseAsync: Deferred<Enphase>,
) : ViewModel() {
  private suspend fun enphase() = enphaseAsync.await()
  private suspend fun settings() = db.settingsDao().getSettings()

  val liveStatusFlow: StateFlow<LiveStatus> = flow {
    val settings = settings() ?: return@flow
    enphase().streamLiveStatus(settings.email, settings.mainGateway, settings.exportGateway).collect {
      Timber.log(Log.DEBUG, "$it")
      emit(it)
    }
  }.stateIn(viewModelScope, LiveStatus(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0))
}