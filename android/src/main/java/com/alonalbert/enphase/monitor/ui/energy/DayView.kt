package com.alonalbert.enphase.monitor.ui.energy

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.repository.DayData
import com.alonalbert.enphase.monitor.ui.battery.BatteryLevelChart
import com.alonalbert.enphase.monitor.ui.energy.ProductionSplit.EXPORT
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme

@Composable
fun DayView(
  dayData: DayData,
  reserveConfig: ReserveConfig,
  batteryCapacity: Double,
) {
  var showProduction by remember { mutableStateOf(true) }
  var showConsumption by remember { mutableStateOf(true) }
  var showStorage by remember { mutableStateOf(true) }
  var showGrid by remember { mutableStateOf(true) }
  var productionSplit by remember { mutableStateOf(EXPORT) }

  Column(modifier = Modifier.padding(horizontal = 8.dp)) {
    TotalEnergy(
      dayData.totalProductionMain,
      dayData.totalProductionExport,
      dayData.totalConsumption,
      dayData.totalCharge,
      dayData.totalDischarge,
      dayData.totalImport,
      dayData.totalExport,
      onProductionClicked = { productionSplit = !productionSplit }
    )
    DailyEnergyChart(dayData, productionSplit, showProduction, showConsumption, showStorage, showGrid)
    BatteryLevelChart(dayData.battery.filterNotNull(), batteryCapacity, reserveConfig = reserveConfig)
    ChartSwitches(
      isProductionChecked = showProduction,
      isConsumptionChecked = showConsumption,
      isStorageChecked = showStorage,
      isGridChecked = showGrid,
      onProductionChanged = { showProduction = !showProduction },
      onConsumptionChanged = { showConsumption = !showConsumption },
      onStorageChanged = { showStorage = !showStorage },
      onGridChanged = { showGrid = !showGrid },
    )
  }
}

@Preview(
  showBackground = true,
  showSystemUi = true,
  device = Devices.PIXEL_7_PRO,
  uiMode = Configuration.UI_MODE_NIGHT_YES,

  )
@Composable
private fun DayViewPreviewDark() {
  SolarCombinerTheme {
    DayView(
      dayData = SampleData.dayData,
      batteryCapacity = 20.16,
      reserveConfig = ReserveConfig.DEFAULT,
    )
  }
}