package com.alonalbert.enphase.monitor.ui.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import com.alonalbert.enphase.monitor.ui.energy.DecimalValueFormatter
import com.alonalbert.enphase.monitor.ui.energy.timeOfDayAxisValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineFill
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineStroke
import kotlinx.coroutines.runBlocking

private const val CHUNK = 5

@Composable
fun ChannelsChart(
  data: List<ChannelUsage>,
  modifier: Modifier = Modifier,
) {
  if (data.isEmpty()) {
    return
  }
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(data) {
    modelProducer.runTransaction(data)
  }
  ChannelsChart(modelProducer, modifier)
}

@Composable
private fun ChannelsChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val colors = channelColors()
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider = LineCartesianLayer.LineProvider.series(
            colors.map {
              LineCartesianLayer.rememberLine(
                fill = LineFill.single(fill(it)),
                stroke = LineStroke.continuous(1.dp),
              )
            }
          ),
          rangeProvider = CartesianLayerRangeProvider.fixed(minX = 0.0, maxX = 1440.0 / CHUNK)
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
            valueFormatter = timeOfDayAxisValueFormatter(60 / CHUNK),
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
    zoomState = rememberVicoZoomState(initialZoom = Zoom.Content),
    modifier = modifier,
  )
}

private suspend fun CartesianChartModelProducer.runTransaction(
  data: List<ChannelUsage>,
) {
  runTransaction {
    lineSeries {
      data.forEach { channelUsage ->
        series(channelUsage.usage.prepare())
      }
    }
  }
}

private fun List<Double>.prepare(): List<Double> {
  return chunked(CHUNK).map { it.average() * 60 }
}

//private class DayMarkerValueFormatter(
//  private val androidContext: Context,
//) : ValueFormatter {
//  override fun format(
//    context: CartesianDrawingContext,
//    targets: List<CartesianMarker.Target>
//  ): CharSequence {
//    with(androidContext) {
//      val solarColor = colorOf(R.color.solar)
//      val gridColor = colorOf(R.color.grid)
//      val batteryColor = colorOf(R.color.battery)
//      val consumptionColor = colorOf(R.color.consumption)
//      return buildSpannedString {
//        targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>().forEach { target ->
//          val columns = target.columns
//          val produced = columns[0].entry.y
//          val consumed = -columns[1].entry.y
//          val grid = columns[2].entry.y
//          val battery = columns[3].entry.y
//          append("${rangeOfChunk(target.x.toInt())}\n")
//          appendEnergyValue("Produced", produced, solarColor)
//          when (grid >= 0) {
//            true -> appendEnergyValue("Imported", grid, gridColor)
//            false -> appendEnergyValue("Exported", -grid, gridColor)
//          }
//          when (battery >= 0) {
//            true -> appendEnergyValue("Discharged", battery, batteryColor)
//            false -> appendEnergyValue("Charged", -battery, batteryColor)
//          }
//          appendEnergyValue("Consumed", consumed, consumptionColor)
//
//          setSpan(TabStopSpan.Standard(100), 0, SpannableStringBuilder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
//        }
//      }
//    }
//  }
//}

@Composable
private fun channelColors() = listOf(
  colorResource(R.color.channel_01),
  colorResource(R.color.channel_02),
  colorResource(R.color.channel_03),
  colorResource(R.color.channel_04),
  colorResource(R.color.channel_05),
  colorResource(R.color.channel_06),
  colorResource(R.color.channel_07),
  colorResource(R.color.channel_08),
  colorResource(R.color.channel_09),
  colorResource(R.color.channel_10),
  colorResource(R.color.channel_11),
  colorResource(R.color.channel_12),
  colorResource(R.color.channel_13),
  colorResource(R.color.channel_14),
  colorResource(R.color.channel_15),
  colorResource(R.color.channel_16),
)

@Composable
@Preview
private fun Preview() {
  val data = sampleEmporiaData()
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    val modelProducer = CartesianChartModelProducer()
    runBlocking {
      modelProducer.runTransaction(data)
    }
    ChannelsChart(modelProducer)
  }
}
