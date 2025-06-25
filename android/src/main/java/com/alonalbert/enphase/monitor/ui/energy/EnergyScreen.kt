package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.ui.datepicker.DayPicker
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import com.alonalbert.solarsim.simulator.DailyEnergy
import java.time.LocalDate

@Composable
fun EnergyScreen(onLogout: () -> Unit) {
  val viewModel: EnergyViewModel = hiltViewModel()

  LaunchedEffect(Unit) {
    viewModel.setDay(LocalDate.now())
  }
  val dailyEnergy by viewModel.dailyEnergyState.collectAsStateWithLifecycle()


  dailyEnergy?.let { EnergyScreen(it, { viewModel.setDay(it) }, onLogout) }

}

@Composable
fun EnergyScreen(dailyEnergy: DailyEnergy, onDayChanged: (LocalDate) -> Unit, onLogout: () -> Unit) {
  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {

      DayPicker(dailyEnergy.date, onDayChanged)
      DailyEnergyChart(dailyEnergy)

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