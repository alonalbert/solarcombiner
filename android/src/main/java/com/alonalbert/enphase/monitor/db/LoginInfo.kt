package com.alonalbert.enphase.monitor.db

import com.alonalbert.enphase.monitor.enphase.model.GatewayConfig

data class LoginInfo(
  val email: String,
  val password: String,
  val mainSiteId: String,
  val mainSerialNumber: String,
  val mainHost: String,
  val mainPort: Int,
  val exportSiteId: String,
  val exportSerialNumber: String,
  val exportHost: String,
  val exportPort: Int,
) {
  fun isValid(): Boolean {
    if (email.isBlank() || password.isBlank()) {
      return false
    }
    if (mainSiteId.isBlank() || mainSerialNumber.isBlank() || mainHost.isBlank() || mainPort <= 0) {
      return false
    }
    if (exportSiteId.isBlank() && exportSerialNumber.isBlank() && exportHost.isBlank()) {
      return true
    }
    if (exportSiteId.isBlank() || exportSerialNumber.isBlank() || exportHost.isBlank() || exportPort <= 0) {
      return false
    }
    return true
  }
}

val LoginInfo.mainGateway get() = GatewayConfig(mainSiteId, mainSerialNumber, mainHost, mainPort)
val LoginInfo.exportGateway get() =
  when {
    exportSiteId.isBlank() -> null
    exportSerialNumber.isBlank() -> null
    exportHost.isBlank() -> null
    exportPort < 0 -> null
    else -> GatewayConfig(exportSiteId, exportSerialNumber, exportHost, exportPort)
  }

