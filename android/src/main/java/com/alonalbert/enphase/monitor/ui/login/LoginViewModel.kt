package com.alonalbert.enphase.monitor.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.settings.EMAIL
import com.alonalbert.enphase.monitor.settings.EXPORT_HOST
import com.alonalbert.enphase.monitor.settings.EXPORT_PORT
import com.alonalbert.enphase.monitor.settings.EXPORT_SERIAL_NUM
import com.alonalbert.enphase.monitor.settings.EXPORT_SITE_ID
import com.alonalbert.enphase.monitor.settings.MAIN_HOST
import com.alonalbert.enphase.monitor.settings.MAIN_PORT
import com.alonalbert.enphase.monitor.settings.MAIN_SERIAL_NUM
import com.alonalbert.enphase.monitor.settings.MAIN_SITE_ID
import com.alonalbert.enphase.monitor.settings.PASSWORD
import com.alonalbert.enphase.monitor.settings.dataStore
import com.alonalbert.enphase.monitor.settings.updateSettings
import com.alonalbert.enphase.monitor.util.stateIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val application: TheApplication,
) : ViewModel() {

  val loginInfo = application.dataStore.data.map {
    EnphaseConfig(it)
  }.stateIn(viewModelScope, EnphaseConfig())

  fun login(loginInfo: EnphaseConfig, onLoggedIn: () -> Unit) {
    viewModelScope.launch {
      application.updateSettings(viewModelScope) {
        set(EMAIL, loginInfo.email)
        set(PASSWORD, loginInfo.password)
        set(MAIN_SITE_ID, loginInfo.mainSiteId)
        set(MAIN_SERIAL_NUM, loginInfo.mainSerialNum)
        set(MAIN_HOST, loginInfo.mainHost)
        set(MAIN_PORT, loginInfo.mainPort)
        set(EXPORT_SITE_ID, loginInfo.exportSiteId)
        set(EXPORT_SERIAL_NUM, loginInfo.exportSerialNum)
        set(EXPORT_HOST, loginInfo.exportHost)
        set(EXPORT_PORT, loginInfo.exportPort)
      }
      onLoggedIn()
    }
  }
}
