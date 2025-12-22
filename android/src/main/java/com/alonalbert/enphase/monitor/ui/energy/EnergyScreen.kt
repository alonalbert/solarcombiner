package com.alonalbert.enphase.monitor.ui.energy

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.repository.ChartData
import com.alonalbert.enphase.monitor.repository.DayData
import com.alonalbert.enphase.monitor.repository.MonthData
import com.alonalbert.enphase.monitor.ui.battery.BatteryBar
import com.alonalbert.enphase.monitor.ui.channels.ChannelsChart
import com.alonalbert.enphase.monitor.ui.channels.sampleEmporiaData
import com.alonalbert.enphase.monitor.ui.components.PullToRefresh
import com.alonalbert.enphase.monitor.ui.datepicker.DayPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.DayPicker
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPeriod
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPicker
import com.alonalbert.enphase.monitor.ui.datepicker.Period
import com.alonalbert.enphase.monitor.ui.datepicker.PeriodPicker
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration.Companion.minutes

@Composable
fun EnergyScreen(
  onSettings: () -> Unit,
  onLiveStatus: () -> Unit,
  onReserve: () -> Unit,
  showSnackbar: suspend (String) -> Unit,
) {
  val viewModel: EnergyViewModel = hiltViewModel()
  val lifecycleOwner = LocalLifecycleOwner.current

  LaunchedEffect(lifecycleOwner, viewModel) {
    viewModel.setPeriod(DayPeriod(viewModel.today()))
    lifecycleOwner.lifecycle.repeatOnLifecycle(STARTED) {
      while (true) {
        viewModel.refreshData()
        delay(5.minutes)
      }
    }
  }
  val chartData by viewModel.chartDataFlow.collectAsStateWithLifecycle()
  val channelData by viewModel.channelDataFlow.collectAsStateWithLifecycle()
  val batteryState by viewModel.batteryStateState.collectAsStateWithLifecycle()
  val reserveConfig by viewModel.reserveConfigState.collectAsStateWithLifecycle()
  val batteryCapacity by viewModel.batteryCapacity.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
  val snackBarMessage by viewModel.snackbarMessageState.collectAsStateWithLifecycle()

  EnergyScreen(
    chartData = chartData,
    channelData = channelData,
    batteryState = batteryState,
    reserveConfig = reserveConfig,
    batteryCapacity = batteryCapacity,
    viewModel.today(),
    snackbarMessage = snackBarMessage,
    onDismissSnackbar = { viewModel.dismissSnackbarMessage() },
    onPeriodChanged = { viewModel.setPeriod(it) },
    onSettings = onSettings,
    onLiveStatus = onLiveStatus,
    onReserve = onReserve,
    showSnackbar = showSnackbar,
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.refreshData() },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyScreen(
  chartData: ChartData,
  channelData: List<ChannelUsage>,
  batteryState: BatteryState,
  reserveConfig: ReserveConfig,
  batteryCapacity: Double,
  today: LocalDate,
  snackbarMessage: String?,
  onDismissSnackbar: () -> Unit,
  onPeriodChanged: (Period) -> Unit,
  onSettings: () -> Unit,
  onLiveStatus: () -> Unit,
  onReserve: () -> Unit,
  showSnackbar: suspend (String) -> Unit,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
) {
  val pullRefreshState = rememberPullToRefreshState()
  PullToRefresh(
    modifier = Modifier.fillMaxSize(),
    state = pullRefreshState,
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
  ) {
    var dataMode by remember { mutableStateOf(DataMode.ENPHASE) }

    val scrollState = rememberScrollState()
    Column(
      modifier = Modifier
        .height(maxHeight)
        .padding(horizontal = 8.dp)
        .verticalScroll(scrollState)
    ) {


      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        PeriodPicker(chartData.period, today, onPeriodChanged)
        Button(onClick = { dataMode = dataMode.next() }) {
          Text(dataMode.name)
        }
        TopBar(onSettings, onReserve, onLiveStatus)
      }

      when (val period = chartData.period) {
        is DayPeriod -> DayPicker(period.day, today, { onPeriodChanged(DayPeriod(it)) })
        is MonthPeriod -> MonthPicker(period.month, { onPeriodChanged(MonthPeriod(it)) })
      }

      when (dataMode) {
        DataMode.ENPHASE -> EnphaseData(chartData, batteryState, reserveConfig, batteryCapacity)
        DataMode.EMPORIA -> EmporiaData(channelData)
      }
    }
  }

  if (snackbarMessage != null) {
    LaunchedEffect(snackbarMessage) {
      showSnackbar(snackbarMessage)
      onDismissSnackbar()
    }
  }
}

private enum class DataMode {
  ENPHASE,
  EMPORIA,
  ;

  fun next() = entries[(ordinal + 1) % entries.size]
}


@Composable
private fun EmporiaData(
  channelData: List<ChannelUsage>,
) {
  ChannelsChart(channelData)
}

@Composable
private fun ColumnScope.EnphaseData(
  chartData: ChartData,
  batteryState: BatteryState,
  reserveConfig: ReserveConfig,
  batteryCapacity: Double,
) {
  var showProduction by remember { mutableStateOf(true) }
  var showConsumption by remember { mutableStateOf(true) }
  var showStorage by remember { mutableStateOf(true) }
  var showGrid by remember { mutableStateOf(true) }

  Box(contentAlignment = Center, modifier = Modifier.fillMaxWidth()) {
    BatteryBar(batteryState.soc ?: 0, batteryCapacity, batteryState.reserve ?: 0)
  }

  Box(modifier = Modifier.weight(1f)) {
    when (chartData) {
      is DayData -> DayView(
        chartData,
        reserveConfig,
        batteryCapacity,
        showProduction,
        showConsumption,
        showStorage,
        showGrid
      )

      is MonthData -> MonthView(
        chartData,
        showProduction,
        showConsumption,
        showStorage,
        showGrid
      )
    }
  }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
  onSettingsClick: () -> Unit,
  onReserveClick: () -> Unit,
  onLiveStatusClick: () -> Unit,
) {
  Row {
    IconButton(onClick = onSettingsClick) {
      Icon(
        imageVector = Icons.Filled.Settings,
        tint = MaterialTheme.colorScheme.onBackground,
        contentDescription = stringResource(id = R.string.settings),
      )
    }
    IconButton(onClick = onReserveClick) {
      Icon(
        imageVector = Icons.Filled.Power,
        tint = MaterialTheme.colorScheme.onBackground,
        contentDescription = stringResource(id = R.string.settings),
      )
    }
    IconButton(onClick = onLiveStatusClick) {
      Icon(
        imageVector = Icons.Filled.CenterFocusWeak,
        tint = MaterialTheme.colorScheme.onBackground,
        contentDescription = stringResource(id = R.string.live_status),
      )
    }
  }
}

@Preview(
  showBackground = true,
  showSystemUi = true,
  device = "spec:width=1080px,height=2424px,dpi=400,navigation=buttons"
)
@Composable
fun GreetingPreviewLight() {
  SolarCombinerTheme {
    Scaffold {
      Box(modifier = Modifier.padding(it)) {
        EnergyScreen(
          chartData = SampleData.dayData,
          sampleEmporiaData(),
          batteryState = BatteryState(null, null),
          reserveConfig = ReserveConfig.DEFAULT,
          batteryCapacity = 20.16,
          LocalDate.now(),
          snackbarMessage = null,
          onDismissSnackbar = {},
          onPeriodChanged = {},
          onSettings = {},
          onReserve = {},
          showSnackbar = {},
          onLiveStatus = {},
          isRefreshing = false,
        ) {}
      }
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
fun GreetingPreviewDark() {
  SolarCombinerTheme {
    Scaffold {
      Box(modifier = Modifier.padding(it)) {
        EnergyScreen(
          chartData = MonthData(YearMonth.now(), SampleData.days),
          sampleEmporiaData(),
          batteryState = BatteryState(null, null),
          reserveConfig = ReserveConfig.DEFAULT,
          batteryCapacity = 20.16,
          LocalDate.now(),
          snackbarMessage = null,
          onDismissSnackbar = {},
          onPeriodChanged = {},
          onSettings = {},
          onReserve = {},
          showSnackbar = {},
          onLiveStatus = {},
          isRefreshing = false,
        ) {}
      }
    }
  }
}