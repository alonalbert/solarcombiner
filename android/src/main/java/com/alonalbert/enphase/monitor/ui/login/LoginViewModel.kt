package com.alonalbert.enphase.monitor.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.LoginInfo
import com.alonalbert.enphase.monitor.repository.Repository
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val repository: Repository,
  private val db: AppDatabase,
) : ViewModel() {
  val loginInfo = db.loginInfoDao().flow().stateIn(viewModelScope, null)

  fun login(loginInfo: LoginInfo, onLoggedIn: () -> Unit) {
    viewModelScope.launch {
      repository.updateEnphaseConfig(loginInfo)
      db.loginInfoDao().update(loginInfo)
      onLoggedIn()
    }
  }
}
