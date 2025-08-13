package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.repository.MonthData
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import java.time.YearMonth

@Composable
fun MonthView(
  monthData: MonthData,
  showProduction: Boolean,
  showConsumption: Boolean,
  showStorage: Boolean,
  showGrid: Boolean,
) {
  Column(modifier = Modifier.padding(horizontal = 8.dp)) {
    val days = monthData.days
    TotalEnergy(
      days.sumOf { it.production },
      days.sumOf { it.exportProduction },
      days.sumOf { it.consumption },
      days.sumOf { it.charge },
      days.sumOf { it.discharge },
      days.sumOf { it.import },
      days.sumOf { it.export },
    )
    MonthChart(days, showProduction, showConsumption, showStorage, showGrid)
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
      showProduction = true,
      showConsumption = true,
      showStorage = true,
      showGrid = true,
    )
  }
}