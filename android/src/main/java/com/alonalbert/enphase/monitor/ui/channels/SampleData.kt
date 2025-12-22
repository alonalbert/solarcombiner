package com.alonalbert.enphase.monitor.ui.channels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import kotlinx.serialization.json.Json

@Composable
fun sampleEmporiaData(): List<ChannelUsage> {
  val context = LocalContext.current
  val json = remember(context) {
    context.assets.open("emporia-sample.json").bufferedReader().use { it.readText() }
  }
  return Json.decodeFromString(json)
}
