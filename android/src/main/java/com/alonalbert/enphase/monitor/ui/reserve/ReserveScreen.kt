package com.alonalbert.enphase.monitor.ui.reserve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import com.alonalbert.enphase.monitor.ui.battery.BatteryLevelChart
import com.alonalbert.enphase.monitor.ui.components.HeadingTextComponent
import com.alonalbert.enphase.monitor.ui.components.PresetEditField
import java.time.LocalTime

@Composable
fun ReserveScreen(
  onUpdateClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val viewModel: ReserveConfigViewModel = hiltViewModel()
  val reserveConfig by viewModel.reserveConfig.collectAsStateWithLifecycle(null)

  val config = reserveConfig
  if (config != null) ReserveScreen(
    reserveConfig = config,
    batteryCapacity = 20.16,
    onUpdate = {
      viewModel.update(it)
      onUpdateClicked()
    },
    modifier = modifier
  )
}

@Composable
fun ReserveScreen(
  reserveConfig: ReserveConfig,
  batteryCapacity: Double,
  onUpdate: (ReserveConfig) -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize(),
  ) { innerPadding ->
    Box(
      contentAlignment = Alignment.Center,
      modifier = modifier
        .padding(innerPadding)
        .fillMaxSize(),
    ) {
      var idleLoad by remember { mutableDoubleStateOf(reserveConfig.idleLoad) }
      var minReserve by remember { mutableIntStateOf(reserveConfig.minReserve) }
      var chargeTime by remember { mutableIntStateOf(reserveConfig.chargeStart) }
      var chartConfig by remember { mutableStateOf(reserveConfig) }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp),
      ) {
        HeadingTextComponent(value = stringResource(id = R.string.reserve_config))
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val weight = Modifier.weight(1f)
          IdleLoad(value = idleLoad, onValueChange = { idleLoad = it }, modifier = weight)
          MinReserve(value = minReserve, onValueChange = { minReserve = it }, modifier = weight)
          SelfConsumptionTime(value = chargeTime, onValueChange = { chargeTime = it }, modifier = weight)
        }
        Spacer(modifier.height(16.dp))
        ChartTitle()
        chartConfig = ReserveConfig(
          idleLoad = idleLoad,
          minReserve = minReserve,
          chargeStart = chargeTime
        )

        BatteryChart(chartConfig, batteryCapacity)
        Spacer(modifier.height(16.dp))
        Button(
          onClick = {
            val config = ReserveConfig(
              idleLoad = idleLoad,
              minReserve = minReserve,
              chargeStart = chargeTime,
            )
            onUpdate(config)
          }) {
          Text("Update", fontSize = 20.sp)
        }
      }
    }
  }
}

@Composable
fun IdleLoad(
  value: Double,
  onValueChange: (Double) -> Unit,
  modifier: Modifier = Modifier,
) {
  PresetEditField(
    label = stringResource(id = R.string.idle_load),
    value = value.toString(),
    onValueChange = { onValueChange(it.toDouble()) },
    presets = (1..30 step 1).map { (it.toDouble() / 10).toString() },
    valueValidator = { it.toDoubleOrNull() != null },
    keyboardType = KeyboardType.Number,
    modifier = modifier,
    maxDropdownHeight = 400.dp,
  )
}

@Composable
fun MinReserve(
  value: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  PresetEditField(
    label = stringResource(id = R.string.min_reserve),
    value = value.toString(),
    onValueChange = { onValueChange(it.toInt()) },
    presets = (5..100 step 5).map { it.toString() },
    valueValidator = { it.toIntOrNull() != null },
    keyboardType = KeyboardType.Number,
    modifier = modifier,
    maxDropdownHeight = 400.dp,
  )
}

@Composable
fun SelfConsumptionTime(
  value: Int,
  onValueChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  PresetEditField(
    label = stringResource(id = R.string.self_consumption_time),
    value = "$value:00",
    onValueChange = { onValueChange(it.split(':')[0].toInt()) },
    presets = (6..12).map { "$it:00" },
    valueValidator = { it.split(':')[0].toIntOrNull() != null },
    keyboardType = KeyboardType.Number,
    modifier = modifier,
    maxDropdownHeight = 400.dp,
  )
}

@Composable
private fun BatteryChart(reserveConfig: ReserveConfig, batteryCapacity: Double) {
  val reserves = (0..95).map {
    val time = LocalTime.of(
      /* hour = */ it / 4,
      /* minute = */ 15 * (it % 4),
    )
    ReserveCalculator.calculateReserve(time, reserveConfig.idleLoad, batteryCapacity, reserveConfig.minReserve, reserveConfig.chargeStart)
  }
  BatteryLevelChart(reserves, modifier = Modifier.height(200.dp))
}

@Composable
private fun ChartTitle() {
  Text(
    text = "Visualization",
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(),
    style = TextStyle(
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      fontStyle = FontStyle.Normal
    ),
    color = colorScheme.onBackground,
    textAlign = TextAlign.Center
  )
}

@Preview(widthDp = 400, heightDp = 800, showBackground = true)
@Composable
fun ReserveScreenPreview() {
  ReserveScreen(
    ReserveConfig(),
    batteryCapacity = 20.0,
    {},
  )
}