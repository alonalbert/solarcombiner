package com.alonalbert.enphase.monitor.ui.energy

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.ui.theme.colorOf
import com.alonalbert.enphase.monitor.util.px
import com.alonalbert.enphase.monitor.util.toDisplay
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import com.alonalbert.solar.combiner.enphase.util.round1

@Composable
fun TotalEnergy(dailyEnergy: DailyEnergy) {
  Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      EnergyBox("Imported", R.drawable.grid, R.color.grid, dailyEnergy.imported)
      val breakdown = "${dailyEnergy.mainProduced.round1} + ${dailyEnergy.exportProduced.round1}"
      EnergyBox("Produced", R.drawable.solar, R.color.solar, dailyEnergy.produced, breakdown)
      EnergyBox("Discharged", R.drawable.battery, R.color.battery, dailyEnergy.discharged)
    }
    ConsumedBox(dailyEnergy)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      EnergyBox("Exported", R.drawable.grid, R.color.grid, dailyEnergy.exported)
      when (dailyEnergy.netImported > 0) {
        true -> EnergyBox("Net Imported", R.drawable.net_import, R.color.consumption, dailyEnergy.netImported)
        false -> EnergyBox("Net Exported", R.drawable.net_export, R.color.solar, -dailyEnergy.netImported)
      }
      EnergyBox("Charged", R.drawable.battery, R.color.battery, dailyEnergy.charged)
    }
  }
}

@Composable
private fun ConsumedBox(dailyEnergy: DailyEnergy) {
  val total = dailyEnergy.produced + dailyEnergy.imported + dailyEnergy.discharged
  val producedAngle = ((dailyEnergy.produced / total) * 360).toFloat()
  val dischargedAngle = ((dailyEnergy.discharged / total) * 360).toFloat()
  val importedAngle = 360 - producedAngle - dischargedAngle
  Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
    with (LocalContext.current) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawArc(colorOf(R.color.solar), -90f, producedAngle, useCenter = false, size = size, style = Stroke(6.dp.px))
        drawArc(colorOf(R.color.battery), -90 + producedAngle, dischargedAngle, useCenter = false, size = size, style = Stroke(6.dp.px))
        drawArc(colorOf(R.color.grid), -90 - importedAngle, importedAngle, useCenter = false, size = size, style = Stroke(6.dp.px))
      }
      Column(horizontalAlignment = CenterHorizontally) {
        Image(
          painterResource(R.drawable.house),
          contentDescription = null,
          modifier = Modifier.size(40.dp),
        )
        Text(dailyEnergy.consumed.toDisplay("kWh", valueWeight = Bold), color = colorOf(R.color.consumption))
      }
    }
  }
}

@Composable
private fun EnergyBox(
  name: String,
  @DrawableRes iconRes: Int,
  @ColorRes colorRes: Int,
  value: Double,
  subName: String? = null,
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
        color = colorResource(colorRes),
      )
      Text(name, color = colorResource(colorRes))
      if (subName != null) {
        Text(subName, color = colorResource(colorRes), fontSize = 12.sp)
      }
    }
  }
}

@Composable
@Preview(widthDp = 400, heightDp = 800)
private fun TotalEnergyPreview() {
  Box(
    modifier = Modifier
      .padding(16.dp)
  ) {
    TotalEnergy(SampleData.sampleData)
  }
}
