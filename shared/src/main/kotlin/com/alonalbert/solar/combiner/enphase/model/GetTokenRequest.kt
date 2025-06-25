package com.alonalbert.solar.combiner.enphase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetTokenRequest(
  @SerialName("session_id")
  val sessionId: String,
  @SerialName("serial_num")
  val serialNum: String,
  @SerialName("username")
  val username: String,
)
