package com.alonalbert.enphase.monitor.ui.channels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import com.alonalbert.enphase.monitor.enphase.util.kw
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import kotlinx.serialization.json.Json

@Composable
fun ChannelUsageScreen() {

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChannelUsageScreenPreview() {
  val sampleData = sampleData()
  SolarCombinerTheme {
    Scaffold { paddingValues ->
      Box(modifier = Modifier.padding(paddingValues)) {
        Column {
          sampleData.forEach {
            Text("Channel: ${it.name}: ${it.usage.sum().kw}")
          }
        }
      }
    }
  }
}

@Composable
private fun sampleData(): List<ChannelUsage> {
  val context = LocalContext.current
  val json = remember(context) {
    context.assets.open("emporia-sample.json").bufferedReader().use { it.readText() }
  }
  return Json.decodeFromString(json)
}