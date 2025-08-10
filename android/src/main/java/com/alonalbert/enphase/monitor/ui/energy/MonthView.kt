package com.alonalbert.enphase.monitor.ui.energy

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
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.repository.MonthData
import com.alonalbert.enphase.monitor.ui.battery.BatteryBar
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPicker
import com.alonalbert.enphase.monitor.ui.datepicker.Period
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import java.time.YearMonth

@Composable
fun MonthView(
  monthData: MonthData,
  batteryState: BatteryState,
  onPeriodChanged: (Period) -> Unit,
) {
  LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
    item {
      MonthPicker(monthData.month, { onPeriodChanged(MonthPeriod(it)) })
    }
    item {
      Box(
        contentAlignment = Center, modifier = Modifier
          .fillMaxWidth()
      ) {
        BatteryBar(batteryState.soc ?: 0, 20.0, batteryState.reserve ?: 0)
      }
    }
    val days = monthData.days
    item {
      TotalEnergy(
        days.sumOf { it.production },
        days.sumOf { it.exportProduction },
        days.sumOf { it.consumption },
        days.sumOf { it.charge },
        days.sumOf { it.discharge },
        days.sumOf { it.import },
        days.sumOf { it.export },
      )
    }
    item {
      MonthChart(days)
    }
  }
}

@Preview(
  showBackground = true,
  showSystemUi = true,
  device = Devices.PIXEL_7_PRO,
)
@Composable
private fun MonthViewPreview() {
  SolarCombinerTheme {
    MonthView(
      monthData = MonthData(YearMonth.now(), SampleData.days),
      batteryState = BatteryState(null, null),
      onPeriodChanged = {},
    )
  }
}