package com.alonalbert.enphase.monitor.emporia.model

import kotlinx.serialization.Serializable

@Serializable
data class EmporiaChannel(
  val deviceGid: Int,
  val name: String,
  val channelNum: String,
  val channelMultiplier: Double,
  val channelTypeGid: Int,
  val parentChannelNum: String?,
  val type: String,
  val channelId: String,
  val mergedChannelId: String?
)