package com.alonalbert.enphase.monitor.ui.reserve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReserveConfigViewModel @Inject constructor(
  private val db: AppDatabase,
) : ViewModel() {
  val reserveConfig = db.reserveConfigDao().getReserveConfigFlow().mapNotNull { it }.stateIn(viewModelScope, ReserveConfig())

  fun update(reserveConfig: ReserveConfig) {
    viewModelScope.launch {
      db.reserveConfigDao().set(reserveConfig)
    }
  }
}
