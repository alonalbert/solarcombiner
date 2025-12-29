package com.alonalbert.enphase.monitor.ui.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.enphase.util.kw

@Composable
fun ChannelSwitches(
  switches: List<SwitchInfo>,
  onChanged: (Int, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = channelColors()
  Box(modifier = modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      switches.chunked(4).forEach { row ->
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = modifier.fillMaxWidth()) {
          row.forEach { switch ->
            ChannelSwitch(
              switch.label,
              switch.kw,
              colors[switch.index],
              switch.state.value,
              { onChanged(switch.index, it) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChannelSwitch(
  label: String,
  kw: Double,
  color: Color,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Center,
  ) {
    val colors = SwitchDefaults.colors()
    Switch(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = color,
        checkedTrackColor = colors.uncheckedTrackColor,
        checkedBorderColor = colors.uncheckedBorderColor,
      )
    )
    Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
    Text(text = kw.kw, style = MaterialTheme.typography.labelSmall, color = color)
  }
}

@Preview(showBackground = true)
@Composable
private fun ChannelSwitchesPreview() {
  val switches = List(16) {
    SwitchInfo(it, "Channel $it", it.toDouble(), remember { mutableStateOf(true) })
  }
  ChannelSwitches(
    switches,
    { i, checked -> switches[i].state.value = checked },
    modifier = Modifier.padding(vertical = 8.dp)
  )
}

@Preview(showBackground = true)
@Composable
private fun ChannelSwitchPreview() {
  ChannelSwitch(
    label = "Production",
    kw = 12.3,
    color = colorResource(R.color.channel_01),
    isChecked = true,
    onCheckedChange = {}
  )
}
