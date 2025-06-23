package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alonalbert.enphase.monitor.ui.datepicker.DayPicker
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import java.time.LocalDate

@Composable
fun EnergyScreen(modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    var day by remember { mutableStateOf(LocalDate.now()) }

    DayPicker(day, { day = it })
  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SolarCombinerTheme {
    EnergyScreen()
  }
}