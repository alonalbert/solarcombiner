package com.alonalbert.enphase.monitor.emporia.model

import kotlinx.serialization.Serializable

@Serializable
data class ChannelUsage(
  val name: String,
  val usage: List<Double>,
)