package com.alonalbert.enphase.monitor.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.repository.Repository
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.Loading
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedIn
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedOut
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel
@Inject constructor(
  private val db: AppDatabase,
  private val repository: Repository,
) : ViewModel() {

  val loginState: StateFlow<LoginState> = db.loginInfoDao().flow().distinctUntilChanged().map {
    when (it?.isValid()) {
      true -> updateLoginInfo()
      else -> LoggedOut
    }
  }.stateIn(viewModelScope, Loading)

  suspend fun updateLoginInfo(): LoggedIn {
    val loginInfo = db.loginInfoDao().get() ?: return LoggedIn
    try {
      repository.updateEnphaseConfig(loginInfo)
      Timber.i("Updated Enphase config")
    } catch (e: Exception) {
      Timber.w(e, "Failed to update Enphase config")
    }
    return LoggedIn
  }

  fun updateReserveConfig(reserveConfig: ReserveConfig) {
    viewModelScope.launch {
      repository.updateReserveConfig(reserveConfig)
    }
  }


  sealed class LoginState {
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
    object Loading : LoginState()
  }
}
