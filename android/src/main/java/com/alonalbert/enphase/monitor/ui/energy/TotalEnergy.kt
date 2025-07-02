package com.alonalbert.enphase.monitor.ui.energy

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.util.toDisplay
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy

@Composable
fun TotalEnergy(sampleData: DailyEnergy) {
  Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      EnergyBox("Imported", R.drawable.grid, R.color.grid, sampleData.imported)
      EnergyBox("Produced", R.drawable.solar, R.color.solar, sampleData.produced)
      EnergyBox("Discharged", R.drawable.battery, R.color.battery, sampleData.discharged)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      EnergyBox("Exported", R.drawable.grid, R.color.grid, sampleData.exported)
      when (sampleData.netImported > 0) {
        true -> EnergyBox("Net Imported", R.drawable.net_import, R.color.consumption, sampleData.netImported)
        false -> EnergyBox("Net Exported", R.drawable.net_export, R.color.solar, -sampleData.netImported)
      }
      EnergyBox("Charged", R.drawable.battery, R.color.battery, sampleData.charged)
    }
  }
}

@Composable
private fun EnergyBox(
  name: String,
  @DrawableRes iconRes: Int,
  @ColorRes colorRes: Int,
  value: Double,
) {
  Row(
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Image(
      painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Column {
      Text(
        value.toDisplay("kWh", valueSize = 14.sp),
        color = colorResource(colorRes),)
      Text(name, color = colorResource(colorRes))
    }
  }
}

@Composable
@Preview
private fun TotalEnergyPreview() {
  Box(
    modifier = Modifier
      .padding(16.dp)
  ) {
    TotalEnergy(SampleData.sampleData)
  }
}
