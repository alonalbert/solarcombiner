package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.exportGateway
import com.alonalbert.enphase.monitor.db.mainGateway
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.LiveStatus
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
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
      emit(it)
    }
  }.stateIn(viewModelScope, LiveStatus(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0))
}