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
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineStroke
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat

private val YDecimalFormat = DecimalFormat("#")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()
private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
  context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

@Composable
fun DailyEnergyChart(
  dailyEnergy: DailyEnergy,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction(dailyEnergy)
  }
  DailyEnergyChart(modelProducer, modifier)
}

private suspend fun CartesianChartModelProducer.runTransaction(dailyEnergy: DailyEnergy) {
  runTransaction {
    columnSeries {
      series(dailyEnergy.energies.map { it.innerProduced + it.outerProduced })
      series(dailyEnergy.energies.map { -it.consumed })
      series(dailyEnergy.energies.map { it.imported - it.innerExported - it.outerProduced })
      series(dailyEnergy.energies.map { it.discharged - it.charged })
    }
    lineSeries { series(dailyEnergy.energies.map { it.innerProduced }) }
    extras {
      it[BottomAxisLabelKey] = (0..95).map { x ->
        when (val h = x / 4) {
          0 -> "12 am"
          12 -> "12 pm"
          else -> (h % 12).toString()
        }
      }
    }
  }
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
              LineCartesianLayer.LineFill.single(fill(Color.DarkGray.copy(alpha = .5f))),
              LineStroke.Continuous(1f)
            )
          ),
          pointSpacing = 2.2.dp,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,
            valueFormatter = StartAxisValueFormatter,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(textSize = 10.sp),
            valueFormatter = BottomAxisValueFormatter,
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
    modifier = modifier.height(252.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
@Preview
private fun Preview() {
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    val modelProducer = remember { CartesianChartModelProducer() }
    // Use `runBlocking` only for previews, which don’t support asynchronous execution.
    val dailyEnergy = SampleData.sampleData
    runBlocking {
      modelProducer.runTransaction(dailyEnergy)
    }
    DailyEnergyChart(modelProducer)
  }
}
