package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.exportGateway
import com.alonalbert.enphase.monitor.db.mainGateway
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.model.LiveStatus
import com.alonalbert.enphase.monitor.repository.Repository
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveStatusViewModel @Inject constructor(
  private val db: AppDatabase,
  private val repository: Repository,
) : ViewModel() {
  private suspend fun settings() = db.settingsDao().getSettings()

  init {
    viewModelScope.launch {
      repository.updateBatteryCapacity()
    }
  }

  val batteryCapacity = db.batteryDao().getBatteryCapacityFlow().mapNotNull { it }.stateIn(viewModelScope, 0.0)
  val liveStatusFlow: StateFlow<LiveStatus> = flow {
    val settings = settings() ?: return@flow
    val enphase = Enphase()
    enphase.ensureLogin(settings.email, settings.password)
    enphase.streamLiveStatus(settings.email, settings.mainGateway, settings.exportGateway).collect {
      emit(it)
    }
  }.stateIn(viewModelScope, LiveStatus(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0))
}