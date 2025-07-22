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
import com.alonalbert.enphase.monitor.ui.battery.BatteryLevelChart
import com.alonalbert.enphase.monitor.ui.components.HeadingTextComponent
import com.alonalbert.enphase.monitor.ui.components.TextFieldComponent
import com.alonalbert.solar.combiner.enphase.ReserveCalculator
import java.time.LocalTime

@Composable
fun ReserveScreen(
  onUpdateClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val viewModel: ReserveConfigViewModel = hiltViewModel()
  val reserveConfig by viewModel.reserveConfig.collectAsStateWithLifecycle(ReserveConfig())
  ReserveScreen(
    reserveConfig = reserveConfig,
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
      var idleLoad by remember { mutableStateOf(reserveConfig.idleLoad.toString()) }
      var minReserve by remember { mutableStateOf(reserveConfig.minReserve.toString()) }
      var chargeTime by remember { mutableStateOf(reserveConfig.chargeTime.toString()) }
      var chartConfig by remember { mutableStateOf(reserveConfig) }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp),
      ) {
        HeadingTextComponent(value = stringResource(id = R.string.reserve_config))
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextFieldComponent(
            text = idleLoad,
            labelValue = stringResource(id = R.string.idle_load),
            onTextChanged = { idleLoad = it },
            keyboardType = KeyboardType.Number,
            isError = false, // TODO
            modifier = Modifier.weight(1f),
          )
          TextFieldComponent(
            text = minReserve,
            labelValue = stringResource(id = R.string.min_reserve),
            onTextChanged = { minReserve = it },
            keyboardType = KeyboardType.Number,
            isError = false, // TODO
            modifier = Modifier.weight(1f),
          )
          TextFieldComponent(
            text = chargeTime,
            labelValue = stringResource(id = R.string.charge_time),
            onTextChanged = { chargeTime = it },
            keyboardType = KeyboardType.Number,
            isError = false, // TODO
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(modifier.height(16.dp))
        ChartTitle()
        val configOk = checkConfig(idleLoad, minReserve, chargeTime)
        if (configOk) {
          chartConfig = ReserveConfig(
            idleLoad = idleLoad.toDouble(),
            minReserve = minReserve.toInt(),
            chargeTime = chargeTime.toInt()
          )

        }

        BatteryChart(chartConfig, batteryCapacity)
        Spacer(modifier.height(16.dp))
        Button(
          enabled = configOk,
          onClick = {
            val config = ReserveConfig(
              idleLoad = idleLoad.toDouble(),
              minReserve = minReserve.toInt(),
              chargeTime = chargeTime.toInt(),
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
private fun BatteryChart(reserveConfig: ReserveConfig, batteryCapacity: Double) {
  val reserves = (0..95).map {
    val time = LocalTime.of(
      /* hour = */ it / 4,
      /* minute = */ 5 * (it % 4),
    )
    ReserveCalculator.calculateReserve(time, reserveConfig.idleLoad, batteryCapacity, reserveConfig.minReserve, reserveConfig.chargeTime)
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

private fun checkConfig(idleLoad: String, minReserve: String, chargeTime: String): Boolean {
  return idleLoad.toDoubleOrNull() != null && minReserve.toIntOrNull() != null && chargeTime.toIntOrNull() != null
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