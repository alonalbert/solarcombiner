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
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel
@Inject constructor(
  db: AppDatabase,
  private val repository: Repository,
) : ViewModel() {

  val loginState: StateFlow<LoginState> = db.loginInfoDao().flow().distinctUntilChanged().map {
    when {
      it == null -> Loading
      it.isValid() -> repository.updateEnphaseConfig(it).let { LoggedIn }
      else -> LoggedOut
    }
  }.stateIn(viewModelScope, Loading)

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
