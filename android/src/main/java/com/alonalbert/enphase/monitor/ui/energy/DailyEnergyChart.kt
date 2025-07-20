package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineStroke
import kotlinx.coroutines.runBlocking

@Composable
fun DailyEnergyChart(
  dailyEnergy: DailyEnergy,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(dailyEnergy) {
    modelProducer.runTransaction(dailyEnergy)
  }
  DailyEnergyChart(modelProducer, modifier)
}

@Composable
private fun DailyEnergyChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(fill = fill(colorResource(R.color.solar)), thickness = 2.2.dp),
              rememberLineComponent(fill = fill(colorResource(R.color.consumption)), thickness = 2.2.dp),
              rememberLineComponent(fill = fill(colorResource(R.color.grid)), thickness = 2.2.dp),
              rememberLineComponent(fill = fill(colorResource(R.color.battery)), thickness = 2.2.dp),
            ),
          columnCollectionSpacing = 0.8.dp,
          mergeMode = { ColumnCartesianLayer.MergeMode.stacked() },
        ),
        rememberLineCartesianLayer(
          lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
              LineCartesianLayer.LineFill.single(fill(Color.Black.copy(alpha = .5f))),
              LineStroke.Continuous(1.5f)
            )
          ),
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
            itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned(
              spacing = { 12 },
              offset = { 0 },
              shiftExtremeLines = false,
              addExtremeLabelPadding = true
            ) },
          ),
        marker = rememberMarker(DailyEnergyValueFormatter(LocalContext.current), lineCount = 4),
        layerPadding = { cartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(300.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

private suspend fun CartesianChartModelProducer.runTransaction(dailyEnergy: DailyEnergy) {
  runTransaction {
    columnSeries {
      series(dailyEnergy.energies.map { it.mainProduced + it.exportProduced })
      series(dailyEnergy.energies.map { -it.consumed })
      series(dailyEnergy.energies.map { it.imported - it.mainExported - it.exportProduced })
      series(dailyEnergy.energies.map { it.discharged - it.charged })
    }
    lineSeries { series(dailyEnergy.energies.map { it.mainProduced }) }
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
      modelProducer.runTransaction(SampleData.sampleData)
    }
    DailyEnergyChart(modelProducer)
  }
}
