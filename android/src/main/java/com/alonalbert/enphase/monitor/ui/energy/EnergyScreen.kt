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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.repository.ChartData
import com.alonalbert.enphase.monitor.repository.DayData
import com.alonalbert.enphase.monitor.repository.MonthData
import com.alonalbert.enphase.monitor.ui.battery.BatteryBar
import com.alonalbert.enphase.monitor.ui.datepicker.Period
import com.alonalbert.enphase.monitor.ui.datepicker.PeriodPicker
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import kotlinx.coroutines.delay
import java.time.YearMonth
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
    viewModel.setPeriod(Period.today())
    lifecycleOwner.lifecycle.repeatOnLifecycle(STARTED) {
      while (true) {
        viewModel.refreshData()
        delay(5.minutes)
      }
    }
  }
  val chartData by viewModel.chartDataFlow.collectAsStateWithLifecycle()
  val batteryState by viewModel.batteryStateState.collectAsStateWithLifecycle()
  val reserveConfig by viewModel.reserveConfigState.collectAsStateWithLifecycle()
  val batteryCapacity by viewModel.batteryCapacity.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
  val snackBarMessage by viewModel.snackbarMessageState.collectAsStateWithLifecycle()

  EnergyScreen(
    chartData = chartData,
    batteryState = batteryState,
    reserveConfig = reserveConfig,
    batteryCapacity = batteryCapacity,
    snackbarMessage = snackBarMessage,
    onDismissSnackbar = { viewModel.dismissSnackbarMessage() },
    onPeriodChanged = { viewModel.setPeriod(it) },
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
  chartData: ChartData,
  batteryState: BatteryState,
  reserveConfig: ReserveConfig,
  batteryCapacity: Double,
  snackbarMessage: String?,
  onDismissSnackbar: () -> Unit,
  onPeriodChanged: (Period) -> Unit,
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
      var showProduction by remember { mutableStateOf(true) }
      var showConsumption by remember { mutableStateOf(true) }
      var showStorage by remember { mutableStateOf(true) }
      var showGrid by remember { mutableStateOf(true) }

      LazyColumn(modifier = Modifier.padding(horizontal = 8.dp)) {
        val data = chartData
        item {
          PeriodPicker(data.period, onPeriodChanged)
        }
        item {
          Box(contentAlignment = Center, modifier = Modifier.fillMaxWidth()) {
            BatteryBar(batteryState.soc ?: 0, batteryCapacity, batteryState.reserve ?: 0)
          }
        }
        item {
          when (data) {
            is DayData -> DayView(data, reserveConfig, batteryCapacity, showProduction, showConsumption, showStorage, showGrid)
            is MonthData -> MonthView(data, showProduction, showConsumption, showStorage, showGrid)
          }
        }
        item {
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
      chartData = SampleData.dayData,
      batteryState = BatteryState(null, null),
      reserveConfig = ReserveConfig.DEFAULT,
      batteryCapacity = 20.16,
      snackbarMessage = null,
      onDismissSnackbar = {},
      onPeriodChanged = {},
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
      chartData = MonthData(YearMonth.now(), SampleData.days),
      batteryState = BatteryState(null, null),
      reserveConfig = ReserveConfig.DEFAULT,
      batteryCapacity = 20.16,
      snackbarMessage = null,
      onDismissSnackbar = {},
      onPeriodChanged = {},
      onSettings = {},
      onReserve = {},
      onLiveStatus = {},
      isRefreshing = false,
    ) {}
  }
}