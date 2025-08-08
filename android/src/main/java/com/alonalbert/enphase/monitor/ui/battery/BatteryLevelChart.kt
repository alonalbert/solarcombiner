package com.alonalbert.enphase.monitor.ui.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import com.alonalbert.enphase.monitor.ui.energy.DecimalValueFormatter
import com.alonalbert.enphase.monitor.ui.energy.SampleData
import com.alonalbert.enphase.monitor.ui.energy.TimeOfDayAxisValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineStroke
import kotlinx.coroutines.runBlocking

@Composable
fun BatteryLevelChart(
  batteryLevels: List<Int>,
  modifier: Modifier = Modifier,
  reserveConfig: ReserveConfig? = null,
) {
  val reserves = when (reserveConfig == null) {
    true -> List(96) { 0 }
    false -> ReserveCalculator.calculateDailyReserves(
      reserveConfig.idleLoad,
      20.16,
      reserveConfig.minReserve,
      reserveConfig.chargeStart
    )
  }
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(batteryLevels) {
    modelProducer.runTransaction(batteryLevels, reserves)
  }
  BatteryLevelChart(modelProducer, modifier)
}

@Composable
private fun BatteryLevelChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val fill = fill(colorResource(R.color.battery_chart))
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
              fill = LineCartesianLayer.LineFill.single(fill),
              areaFill =
                LineCartesianLayer.AreaFill.single(fill)
            ),
            LineCartesianLayer.Line(
              LineCartesianLayer.LineFill.single(fill(
                colorResource(R.color.battery_reserve),
                )),
              LineStroke.Continuous(0.5f)
            )
          ),
          rangeProvider = remember {
            CartesianLayerRangeProvider.fixed(
              minX = 0.0,
              maxX = 95.0,
              minY = 0.0,
              maxY = 100.0
            )
          },
          pointSpacing = 2.2.dp,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,
            valueFormatter = DecimalValueFormatter,
            horizontalLabelPosition = Inside,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(textSize = 10.sp),
            valueFormatter = TimeOfDayAxisValueFormatter,
            guideline = null,
            itemPlacer = remember {
              HorizontalAxis.ItemPlacer.aligned(
                spacing = { 12 },
                offset = { 0 },
                shiftExtremeLines = false,
                addExtremeLabelPadding = true
              )
            },
          ),
        layerPadding = { cartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(120.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

private suspend fun CartesianChartModelProducer.runTransaction(
  batteryLevels: List<Int>,
  reserves: List<Int>,
) {
  runTransaction {
    lineSeries {
      series(batteryLevels)
      series(reserves)
    }
  }
}

@Composable
@Preview
private fun Preview() {
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    val modelProducer = CartesianChartModelProducer()
    runBlocking {
      modelProducer.runTransaction(
        SampleData.sampleData.energies.mapNotNull { it.battery }.subList(0, 50),
        ReserveCalculator.calculateDailyReserves(0.8, 20.16, 20, 9)
      )
    }
    BatteryLevelChart(modelProducer)
  }
}
