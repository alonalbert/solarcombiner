package com.alonalbert.solar.combiner.enphase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
  val message: String,

  @SerialName("session_id")
  val sessionId: String,

  @SerialName("manager_token")
  val managerToken: String,

  @SerialName("is_consumer")
  val isConsumer: Boolean,

  @SerialName("system_id")
  val systemId: Int,
)
