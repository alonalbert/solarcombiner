package com.alonalbert.enphase.monitor.enphase.model

data class GatewayConfig(
  val siteId: String,
  val serialNumber: String,
  val host: String,
  val port: Int,
  ) {
  val url = "https://$host:$port"
}
