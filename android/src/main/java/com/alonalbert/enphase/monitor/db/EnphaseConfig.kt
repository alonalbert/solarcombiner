package com.alonalbert.enphase.monitor.db

import com.alonalbert.enphase.monitor.enphase.model.GatewayConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnphaseConfig(
  val email: String,
  val password: String,
  @SerialName("mainSite")
  val mainSiteId: String,
  @SerialName("mainSerial")
  val mainSerialNumber: String,
  val mainHost: String,
  val mainPort: Int,
  @SerialName("exportSite")
  val exportSiteId: String,
  @SerialName("exportSerial")
  val exportSerialNumber: String,
  val exportHost: String,
  val exportPort: Int,
)

val EnphaseConfig.mainGateway get() = GatewayConfig(mainSiteId, mainSerialNumber, mainHost, mainPort)
val EnphaseConfig.exportGateway
  get() =
    when {
      exportSiteId.isBlank() -> null
      exportSerialNumber.isBlank() -> null
      exportHost.isBlank() -> null
      exportPort < 0 -> null
      else -> GatewayConfig(exportSiteId, exportSerialNumber, exportHost, exportPort)
    }

