package com.alonalbert.enphase.monitor.emporia.model

import kotlinx.serialization.Serializable

@Serializable
data class ChannelUsage(
  val channelId: String,
  val channelName: String,
  val usage: List<Double>,
)