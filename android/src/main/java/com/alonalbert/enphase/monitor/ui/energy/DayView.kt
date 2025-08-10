package com.alonalbert.enphase.monitor.ui.energy

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.ui.battery.BatteryBar
import com.alonalbert.enphase.monitor.ui.battery.BatteryLevelChart
import com.alonalbert.enphase.monitor.ui.datepicker.DayPicker
import com.alonalbert.enphase.monitor.ui.energy.Period.DayPeriod
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme

@Composable
fun DayView(
  dailyEnergy: DailyEnergy,
  batteryState: BatteryState,
  onPeriodChanged: (Period) -> Unit,
  reserveConfig: ReserveConfig,

  ) {
  LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
    item {
      DayPicker(dailyEnergy.date, { onPeriodChanged(DayPeriod(it)) })
    }
    item {
      Box(
        contentAlignment = Center, modifier = Modifier
          .fillMaxWidth()
      ) {
        BatteryBar(batteryState.soc ?: 0, 20.0, batteryState.reserve ?: 0)
      }
    }
    item {
      TotalEnergy(
        dailyEnergy.mainProduced,
        dailyEnergy.exportProduced,
        dailyEnergy.consumed,
        dailyEnergy.charged,
        dailyEnergy.discharged,
        dailyEnergy.imported,
        dailyEnergy.exported,
      )
    }
    item {
      DailyEnergyChart(dailyEnergy)
    }
    item {
      BatteryLevelChart(
        batteryLevels = dailyEnergy.energies.mapNotNull { it.battery },
        reserveConfig = reserveConfig
      )
    }
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
      dailyEnergy = SampleData.sampleData,
      batteryState = BatteryState(null, null),
      reserveConfig = ReserveConfig(),
      onPeriodChanged = {},
    )
  }
}