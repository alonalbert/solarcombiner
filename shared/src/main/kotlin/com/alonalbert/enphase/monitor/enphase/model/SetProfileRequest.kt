package com.alonalbert.enphase.monitor.enphase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SetProfileRequest(
  @SerialName("profile")
  val profile: String,
  @SerialName("batteryBackupPercentage")
  val batteryBackupPercentage: Int,
)
