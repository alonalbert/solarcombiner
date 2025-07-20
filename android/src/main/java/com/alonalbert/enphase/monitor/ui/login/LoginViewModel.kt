package com.alonalbert.enphase.monitor.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.Settings
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val db: AppDatabase,
) : ViewModel() {
  val settings = db.settingsDao().getSettingsFlow().stateIn(viewModelScope, null)

  fun login(settings: Settings, onLoggedIn: () -> Unit) {
    viewModelScope.launch {
      db.settingsDao().set(settings)
      onLoggedIn()
    }
  }
}
