package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun EnergyScreen(onLogout: () -> Unit) {
  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
      var day by remember { mutableStateOf(LocalDate.now()) }

      DayPicker(day, { day = it })
      DailyEnergyChart()

      TextButton(onClick = onLogout) {
        Text("Logout")
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SolarCombinerTheme {
    EnergyScreen {}
  }
}