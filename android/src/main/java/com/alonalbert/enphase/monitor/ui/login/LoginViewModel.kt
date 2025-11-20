package com.alonalbert.enphase.monitor.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.client.Client
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.LoginInfo
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val db: AppDatabase,
) : ViewModel() {
  val loginInfo = db.loginInfoDao().flow().stateIn(viewModelScope, null)

  fun login(loginInfo: LoginInfo, onLoggedIn: () -> Unit) {
    viewModelScope.launch {
      val client = Client(loginInfo.server, loginInfo.username, loginInfo.password)
      val enphaseConfig = client.getEnphaseConfig()
      db.enphaseConfigDao().update(enphaseConfig)
      db.loginInfoDao().update(loginInfo)
      onLoggedIn()
    }
  }
}
