package com.alonalbert.enphase.monitor.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonalbert.enphase.monitor.TheApplication
import com.alonalbert.enphase.monitor.settings.EMAIL
import com.alonalbert.enphase.monitor.settings.INNER_SYSTEM_ID
import com.alonalbert.enphase.monitor.settings.OUTER_SYSTEM_ID
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
    val email = it[EMAIL]
    val password = it[PASSWORD]
    val innerSystemId = it[INNER_SYSTEM_ID]
    val outerSystemId = it[OUTER_SYSTEM_ID]
    when {
      email.isNullOrBlank() -> null
      password.isNullOrBlank() -> null
      innerSystemId.isNullOrBlank() -> null
      outerSystemId.isNullOrBlank() -> null
      else -> LoginInfo(email, password, innerSystemId, outerSystemId)
    }
  }.stateIn(viewModelScope, null)

  fun login(
    email: String,
    password: String,
    innerSystemId: String,
    outerSystemId: String,
    onLoggedIn: () -> Unit,
  ) {
    viewModelScope.launch {
      application.updateSettings(viewModelScope) {
        set(EMAIL, email)
        set(PASSWORD, password)
        set(INNER_SYSTEM_ID, innerSystemId)
        set(OUTER_SYSTEM_ID, outerSystemId)
      }
      onLoggedIn()
    }
  }

  data class LoginInfo(
    val email: String = "",
    val password: String = "",
    val innerSystemId: String = "",
    val outerSystemId: String = "",
  )
}
