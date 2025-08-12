package com.alonalbert.enphase.monitor.ui.energy

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
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
import com.alonalbert.enphase.monitor.enphase.util.round1
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.CHARGE
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.CONSUMPTION
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.DISCHARGE
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.EXPORT
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.IMPORT
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.NET_GRID
import com.alonalbert.enphase.monitor.ui.energy.EnergyType.PRODUCTION
import com.alonalbert.enphase.monitor.ui.theme.colorOf
import com.alonalbert.enphase.monitor.util.px
import com.alonalbert.enphase.monitor.util.toDisplay

@Composable
fun TotalEnergy(
  production: Double,
  productionExport: Double,
  consumption: Double,
  charge: Double,
  discharge: Double,
  import: Double,
  export: Double,
  onClickTotals: (EnergyType) -> Unit = {}
) {
  Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = CenterVertically) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      EnergyBox("Imported", R.drawable.grid, R.color.grid, import) { onClickTotals(IMPORT) }
      val breakdown = "${production.round1} + ${productionExport.round1}"
      EnergyBox("Produced", R.drawable.solar, R.color.solar, production + productionExport, breakdown) { onClickTotals(PRODUCTION) }
      EnergyBox("Discharged", R.drawable.battery, R.color.battery, discharge) { onClickTotals(DISCHARGE) }
    }
    ConsumedBox(consumption, production + productionExport, discharge, import) { onClickTotals(CONSUMPTION) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      EnergyBox("Exported", R.drawable.grid, R.color.grid, export) { onClickTotals(EXPORT) }
      val netImport = import - export
      when (netImport > 0) {
        true -> EnergyBox("Net Imported", R.drawable.net_import, R.color.consumption, netImport, "") { onClickTotals(NET_GRID) }
        false -> EnergyBox("Net Exported", R.drawable.net_export, R.color.solar, -netImport, "") { onClickTotals(NET_GRID) }
      }
      EnergyBox("Charged", R.drawable.battery, R.color.battery, charge) { onClickTotals(CHARGE) }
    }
  }
}

@Composable
private fun ConsumedBox(
  consumption: Double,
  production: Double,
  discharge: Double,
  import: Double,
  onClick: (() -> Unit),
) {
  val total = production + import + discharge
  val producedAngle = ((production / total) * 360).toFloat()
  val dischargedAngle = ((discharge / total) * 360).toFloat()
  val importedAngle = 360 - producedAngle - dischargedAngle
  Box(contentAlignment = Center, modifier = Modifier.size(110.dp).clickable(true, onClick = onClick)) {
    with(LocalContext.current) {
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
        Text(consumption.toDisplay("kWh", valueWeight = Bold), color = colorOf(R.color.consumption))
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
  onClick: (() -> Unit),
) {
  Row(
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.clickable(true, onClick = onClick)
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
    val data = SampleData.dayData
    TotalEnergy(
      data.totalProductionMain,
      data.totalProductionExport,
      data.totalConsumption,
      data.totalCharge,
      data.totalDischarge,
      data.totalImport,
      data.totalExport,
    )
  }
}
