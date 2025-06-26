package com.alonalbert.enphase.monitor.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.settings.LOGGED_IN
import com.alonalbert.enphase.monitor.settings.dataStore
import com.alonalbert.enphase.monitor.settings.updateSettings
import com.alonalbert.enphase.monitor.ui.login.EnphaseConfig
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.Loading
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedIn
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedOut
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
  private val application: TheApplication,
) : ViewModel() {

  val loginState = application.dataStore.data.map {
    when(EnphaseConfig(it).isValid()) {
      true -> LoggedIn
      false -> LoggedOut
    }
  }.stateIn(viewModelScope, Loading)

  fun setLoggedIn(value: Boolean) {
    application.updateSettings(viewModelScope) {
      set(LOGGED_IN, value)
    }
  }

  sealed class LoginState {
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
    object Loading : LoginState()
  }
}
