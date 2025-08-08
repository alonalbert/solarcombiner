package com.alonalbert.enphase.monitor.ui.energy

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.ui.battery.BatteryBar
import com.alonalbert.enphase.monitor.ui.battery.BatteryLevelChart
import com.alonalbert.enphase.monitor.ui.datepicker.DayPicker
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

@Composable
fun EnergyScreen(
  onSettings: () -> Unit,
  onLiveStatus: () -> Unit,
  onReserve: () -> Unit,
) {
  val viewModel: EnergyViewModel = hiltViewModel()
  val lifecycleOwner = LocalLifecycleOwner.current

  LaunchedEffect(lifecycleOwner, viewModel) {
    viewModel.setCurrentDay(LocalDate.now())
    lifecycleOwner.lifecycle.repeatOnLifecycle(STARTED) {
      while (true) {
        viewModel.refreshData()
        delay(5.minutes)
      }
    }
  }
  val dailyEnergy by viewModel.dailyEnergyState.collectAsStateWithLifecycle()
  val batteryState by viewModel.batteryStateState.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
  val snackBarMessage by viewModel.snackbarMessageState.collectAsStateWithLifecycle()

  EnergyScreen(
    dailyEnergy = dailyEnergy,
    batteryState = batteryState,
    snackbarMessage = snackBarMessage,
    onDismissSnackbar = { viewModel.dismissSnackbarMessage() },
    onDayChanged = { date -> viewModel.setCurrentDay(date) },
    onSettings = onSettings,
    onLiveStatus = onLiveStatus,
    onReserve = onReserve,
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.refreshData() },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyScreen(
  dailyEnergy: DailyEnergy,
  batteryState: BatteryState,
  snackbarMessage: String?,
  onDismissSnackbar: () -> Unit,
  onDayChanged: (LocalDate) -> Unit,
  onSettings: () -> Unit,
  onLiveStatus: () -> Unit,
  onReserve: () -> Unit,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
) {
  val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
  val pullRefreshState = rememberPullToRefreshState()
  Scaffold(
    topBar = { TopBar(onSettings, onReserve, onLiveStatus) },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier.fillMaxSize(),
  ) { innerPadding ->
    PullToRefreshBox(
      modifier = Modifier.padding(innerPadding),
      state = pullRefreshState,
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
    ) {
      LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
        item {
          DayPicker(dailyEnergy.date, onDayChanged)
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
          Box(contentAlignment = Center, modifier = Modifier.fillMaxWidth()) {
            TotalEnergy(dailyEnergy)
          }
        }
        item {
          DailyEnergyChart(dailyEnergy)
        }
        item {
          BatteryLevelChart(dailyEnergy.energies.mapNotNull { it.battery })
        }
      }
    }

    val message = snackbarMessage
    if (message != null) {
      LaunchedEffect(snackbarHostState, message) {
        snackbarHostState.showSnackbar(message)
        onDismissSnackbar()
      }
    }
  }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
  onSettingsClick: () -> Unit,
  onReserveClick: () -> Unit,
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
      IconButton(onClick = onReserveClick) {
        Icon(
          imageVector = Icons.Filled.Power,
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

@Preview(
  showBackground = true,
  showSystemUi = true,
  device = Devices.PIXEL_7_PRO
)
@Composable
fun GreetingPreviewLight() {
  SolarCombinerTheme {
    EnergyScreen(
      dailyEnergy = SampleData.sampleData,
      batteryState = BatteryState(null, null),
      snackbarMessage = null,
      onDismissSnackbar = {},
      onDayChanged = {},
      onSettings = {},
      onReserve = {},
      onLiveStatus = {},
      isRefreshing = false,
    ) {}
  }
}

@Preview(
  showBackground = true,
  showSystemUi = true,
  device = Devices.PIXEL_7_PRO,
  uiMode = Configuration.UI_MODE_NIGHT_YES,

  )
@Composable
fun GreetingPreviewDark() {
  SolarCombinerTheme {
    EnergyScreen(
      dailyEnergy = SampleData.sampleData,
      batteryState = BatteryState(null, null),
      snackbarMessage = null,
      onDismissSnackbar = {},
      onDayChanged = {},
      onSettings = {},
      onReserve = {},
      onLiveStatus = {},
      isRefreshing = false,
    ) {}
  }
}