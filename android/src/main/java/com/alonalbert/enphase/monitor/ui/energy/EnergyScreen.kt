package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.ui.datepicker.DayPicker
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import java.time.LocalDate

@Composable
fun EnergyScreen(
  onSettings: () -> Unit,
  onLiveStatus: () -> Unit,
) {
  val viewModel: EnergyViewModel = hiltViewModel()

  LaunchedEffect(Unit) {
    viewModel.setDay(LocalDate.now())
  }
  val dailyEnergy by viewModel.dailyEnergyState.collectAsStateWithLifecycle()


  dailyEnergy?.let {
    EnergyScreen(it, { date -> viewModel.setDay(date) }, onSettings, onLiveStatus)
  }

}

@Composable
fun EnergyScreen(
  dailyEnergy: DailyEnergy,
  onDayChanged: (LocalDate) -> Unit,
  onSettings: () -> Unit,
  onLiveStatus: () -> Unit,
) {
  Scaffold(
    topBar = { TopBar(onSettings, onLiveStatus) },
    modifier = Modifier.fillMaxSize(),
  ) { innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
      DayPicker(dailyEnergy.date, onDayChanged)
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        TotalEnergy(dailyEnergy)
      }
      DailyEnergyChart(dailyEnergy)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
  onSettingsClick: () -> Unit,
  onLiveStatusClick: () -> Unit,
) {
  TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primary,
    ),
    title = {
      Text(
        text = stringResource(R.string.app_name),
        color = MaterialTheme.colorScheme.onPrimary
      )
    },
    actions = {
      IconButton(onClick = onSettingsClick) {
        Icon(
          imageVector = Icons.Filled.Settings,
          tint = MaterialTheme.colorScheme.onPrimary,
          contentDescription = stringResource(id = R.string.settings),
        )
      }
      IconButton(onClick = onLiveStatusClick) {
        Icon(
          imageVector = Icons.Filled.CenterFocusWeak,
          tint = MaterialTheme.colorScheme.onPrimary,
          contentDescription = stringResource(id = R.string.live_status),
        )
      }
    }
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SolarCombinerTheme {
    EnergyScreen(SampleData.sampleData, {}, {}) {}
  }
}