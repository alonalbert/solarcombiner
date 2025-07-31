package com.alonalbert.enphase.monitor.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alonalbert.enphase.monitor.enphase.model.GatewayConfig

@Entity(tableName = "settings")
data class Settings(
  @PrimaryKey
  val id: Int = 1,
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
  constructor(
    email: String,
    password: String,
    mainSiteId: String,
    mainSerialNumber: String,
    mainHost: String,
    mainPort: Int,
    exportSiteId: String,
    exportSerialNumber: String,
    exportHost: String,
    exportPort: Int,
  ) : this(1,  email, password, mainSiteId, mainSerialNumber, mainHost, mainPort, exportSiteId, exportSerialNumber, exportHost, exportPort)

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

val Settings.mainGateway get() = GatewayConfig(mainSiteId, mainSerialNumber, mainHost, mainPort)
val Settings.exportGateway get() =
  when {
    exportSiteId.isBlank() -> null
    exportSerialNumber.isBlank() -> null
    exportHost.isBlank() -> null
    exportPort < 0 -> null
    else -> GatewayConfig(exportSiteId, exportSerialNumber, exportHost, exportPort)
  }

