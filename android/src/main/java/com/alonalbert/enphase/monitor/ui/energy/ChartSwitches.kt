package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.R

@Composable
fun ChartSwitches(
  isProductionChecked: Boolean,
  isConsumptionChecked: Boolean,
  isStorageChecked: Boolean,
  isGridChecked: Boolean,
  onProductionChanged: (Boolean) -> Unit,
  onConsumptionChanged: (Boolean) -> Unit,
  onStorageChanged: (Boolean) -> Unit,
  onGridChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically
  ) {
    ChartSwitch("Production", colorResource(R.color.solar), isProductionChecked, onProductionChanged)
    ChartSwitch("Consumption", colorResource(R.color.consumption), isConsumptionChecked, onConsumptionChanged)
    ChartSwitch("Storage", colorResource(R.color.battery), isStorageChecked, onStorageChanged)
    ChartSwitch("Grid", colorResource(R.color.grid), isGridChecked, onGridChanged)
  }
}

@Composable
private fun ChartSwitch(
  label: String,
  color: Color,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Column(
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
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ChartTogglesPreview() {
  ChartSwitches(
    isProductionChecked = true,
    isConsumptionChecked = true,
    isStorageChecked = false,
    isGridChecked = false,
    onProductionChanged = {},
    onConsumptionChanged = {},
    onStorageChanged = {},
    onGridChanged = {},
    modifier = Modifier.padding(vertical = 8.dp)
  )
}