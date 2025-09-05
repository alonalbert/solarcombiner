package com.alonalbert.enphase.monitor.ui.reserve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.repository.Repository
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReserveConfigViewModel @Inject constructor(
  private val repository: Repository,
  private val db: AppDatabase,
) : ViewModel() {
  init {
    viewModelScope.launch {
      repository.updateBatteryCapacity()
    }
  }

  val reserveConfig = db.batteryDao().getReserveConfigFlow().mapNotNull { it }.stateIn(viewModelScope, ReserveConfig.DEFAULT)
  val batteryCapacity = db.batteryDao().getBatteryCapacityFlow().mapNotNull { it }.stateIn(viewModelScope, 0.0)

  fun update(reserveConfig: ReserveConfig) {
    viewModelScope.launch {
      db.batteryDao().updateReserveConfig(reserveConfig)
    }
  }
}
