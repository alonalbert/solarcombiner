package com.alonalbert.enphase.monitor.ui.login

import androidx.datastore.preferences.core.Preferences
import com.alonalbert.enphase.monitor.settings.email
import com.alonalbert.enphase.monitor.settings.exportHost
import com.alonalbert.enphase.monitor.settings.exportPort
import com.alonalbert.enphase.monitor.settings.exportSerialNum
import com.alonalbert.enphase.monitor.settings.exportSiteId
import com.alonalbert.enphase.monitor.settings.mainHost
import com.alonalbert.enphase.monitor.settings.mainPort
import com.alonalbert.enphase.monitor.settings.mainSerialNum
import com.alonalbert.enphase.monitor.settings.mainSiteId
import com.alonalbert.enphase.monitor.settings.password

data class EnphaseConfig(
  val email: String = "",
  val password: String = "",
  val mainSiteId: String = "",
  val mainSerialNum: String = "",
  val mainHost: String = "",
  val mainPort: Int = 80,
  val exportSiteId: String = "",
  val exportSerialNum: String = "",
  val exportHost: String = "",
  val exportPort: Int = 80,
) {
  constructor(preferences: Preferences) : this(
    preferences.email,
    preferences.password,
    preferences.mainSiteId,
    preferences.mainSerialNum,
    preferences.mainHost,
    preferences.mainPort,
    preferences.exportSiteId,
    preferences.exportSerialNum,
    preferences.exportHost,
    preferences.exportPort,
  )

  fun isValid(): Boolean {
    return email.isNotBlank()
        && password.isNotBlank()
        && mainSiteId.isNotBlank()
        && mainSerialNum.isNotBlank()
        && mainHost.isNotBlank()
        && mainPort > 0
        && exportSiteId.isNotBlank()
        && exportSerialNum.isNotBlank()
        && exportHost.isNotBlank()
        && exportPort > 0
  }
}