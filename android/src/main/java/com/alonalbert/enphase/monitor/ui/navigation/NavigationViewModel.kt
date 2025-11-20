package com.alonalbert.enphase.monitor.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.Loading
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedIn
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedOut
import com.alonalbert.enphase.monitor.util.TimberLogger
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
  private val db: AppDatabase,
) : ViewModel() {

  val loginState: StateFlow<LoginState> = db.loginInfoDao().flow().map {
    when (it?.isValid()) {
      true -> LoggedIn
      else -> LoggedOut
    }
  }.stateIn(viewModelScope, Loading)

  fun updateBatteryReserve(reserveConfig: ReserveConfig) {
    viewModelScope.launch {
      val batteryDao = db.batteryDao()
      batteryDao.updateReserveConfig(reserveConfig)
      val settings = db.settingsDao().get() ?: return@launch
      val enphase = Enphase(TimberLogger())
      enphase.ensureLogin(settings.email, settings.password)
      val mainSiteId = settings.mainSiteId
      val batteryCapacity = enphase.getBatteryCapacity(mainSiteId)
      val reserve = ReserveCalculator.calculateReserve(
        LocalTime.now(),
        reserveConfig.idleLoad,
        batteryCapacity,
        reserveConfig.minReserve,
        reserveConfig.chargeStart,
        reserveConfig.chargeEnd,
      )
      val result = enphase.setBatteryReserve(mainSiteId, reserve)
      batteryDao.updateBatteryReserve(reserve)
      Timber.i("Setting reserve to $reserve ($reserveConfig): $result")
    }
  }

  sealed class LoginState {
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
    object Loading : LoginState()
  }
}
