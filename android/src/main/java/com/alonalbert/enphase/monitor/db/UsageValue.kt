package com.alonalbert.enphase.monitor.db

import kotlinx.serialization.Serializable

@Serializable
data class UsageValue(
  val channelId: Long,
  val value: Double,
)
