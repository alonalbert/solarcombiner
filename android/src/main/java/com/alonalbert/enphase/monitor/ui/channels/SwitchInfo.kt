package com.alonalbert.enphase.monitor.ui.channels

import androidx.compose.runtime.MutableState

data class SwitchInfo(
  val index: Int,
  val label: String,
  val kw: Double,
  val state: MutableState<Boolean>,
)
