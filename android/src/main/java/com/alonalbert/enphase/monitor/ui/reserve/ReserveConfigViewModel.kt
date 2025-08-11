package com.alonalbert.enphase.monitor.ui.reserve

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.services.AlarmReceiver
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReserveConfigViewModel @Inject constructor(
  @param:ApplicationContext private val context: Context,
  private val db: AppDatabase,
) : ViewModel() {
  val reserveConfig = db.batteryDao().getReserveConfigFlow().mapNotNull { it }.stateIn(viewModelScope, ReserveConfig.DEFAULT)

  fun update(reserveConfig: ReserveConfig) {
    viewModelScope.launch {
      db.batteryDao().updateReserveConfig(reserveConfig)
      AlarmReceiver.scheduleAlarm(context)
    }
  }
}
