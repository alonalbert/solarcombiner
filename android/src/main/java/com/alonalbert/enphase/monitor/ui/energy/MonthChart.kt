package com.alonalbert.enphase.monitor.ui.energy

import android.content.Context
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.TabStopSpan
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
import androidx.core.text.buildSpannedString
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.enphase.util.rangeOfChunk
import com.alonalbert.enphase.monitor.ui.theme.colorOf
import com.alonalbert.enphase.monitor.util.appendValue
import com.alonalbert.enphase.monitor.util.seriesOrEmpty
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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer.LineStroke
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker.ValueFormatter
import kotlinx.coroutines.runBlocking

@Composable
fun MonthChart(
  days: List<DayTotals>,
  showProduction: Boolean,
  showConsumption: Boolean,
  showStorage: Boolean,
  showGrid: Boolean,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(days, showProduction, showConsumption, showStorage, showGrid) {
    modelProducer.runTransaction(days, showProduction, showConsumption, showStorage, showGrid)
  }
  MonthChart(modelProducer, modifier)
}

@Composable
private fun MonthChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(fill = fill(colorResource(R.color.solar))),
              rememberLineComponent(fill = fill(colorResource(R.color.grid))),
              rememberLineComponent(fill = fill(colorResource(R.color.battery))),
              rememberLineComponent(fill = fill(colorResource(R.color.consumption))),
              rememberLineComponent(fill = fill(colorResource(R.color.grid))),
              rememberLineComponent(fill = fill(colorResource(R.color.battery))),
            ),
          columnCollectionSpacing = 0.5.dp,
          mergeMode = { ColumnCartesianLayer.MergeMode.stacked() },
        ),
        rememberLineCartesianLayer(
          lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
              LineCartesianLayer.LineFill.single(fill(Color.Gray)),
              LineStroke.Continuous(1f)
            )
          ),
          pointSpacing = 0.5.dp,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,
            valueFormatter = DecimalValueFormatter,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(textSize = 10.sp),
            valueFormatter = CartesianValueFormatter { _, x, _ -> "${(x + 1).toInt()}" },
            guideline = null,
            itemPlacer = remember {
              HorizontalAxis.ItemPlacer.aligned(
                spacing = { 2 },
                offset = { 0 },
                shiftExtremeLines = false,
                addExtremeLabelPadding = true
              )
            },
          ),
        marker = rememberMarker(MonthMarkerValueFormatter(LocalContext.current), lineCount = 7),
        layerPadding = { cartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(300.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

private suspend fun CartesianChartModelProducer.runTransaction(
  days: List<DayTotals>,
  showProduction: Boolean,
  showConsumption: Boolean,
  showStorage: Boolean,
  showGrid: Boolean,
) {
  runTransaction {
    columnSeries {
      days.seriesOrEmpty(showProduction) { it.production + it.exportProduction }
      days.seriesOrEmpty(showGrid) { it.import }
      days.seriesOrEmpty(showStorage) { it.discharge }
      days.seriesOrEmpty(showConsumption) { -it.consumption }
      days.seriesOrEmpty(showGrid) { -it.export }
      days.seriesOrEmpty(showStorage) { -it.charge }
      lineSeries {
        series(List(days.size) { 0 })
      }
    }
  }
}

private class MonthMarkerValueFormatter(
  private val androidContext: Context,
) : ValueFormatter {
  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>
  ): CharSequence {
    with(androidContext) {
      val solarColor = colorOf(R.color.solar)
      val gridColor = colorOf(R.color.grid)
      val storageColor = colorOf(R.color.battery)
      val consumptionColor = colorOf(R.color.consumption)
      return buildSpannedString {
        targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>().forEach { target ->
          val columns = target.columns
          val production = columns[0].entry.y
          val import = columns[1].entry.y
          val discharge = columns[2].entry.y
          val consumption = -columns[3].entry.y
          val export = -columns[4].entry.y
          val charge = -columns[5].entry.y

          append("${rangeOfChunk(target.x.toInt())}\n")
          appendValue("Produced", production, solarColor)
          appendValue("Imported", import, gridColor)
          appendValue("Discharged", discharge, storageColor)
          appendValue("Consumed", consumption, consumptionColor)
          appendValue("Exported", export, gridColor)
          appendValue("Charged", charge, storageColor)

          setSpan(TabStopSpan.Standard(100), 0, length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }
  }
}

@Composable
@Preview(widthDp = 400)
private fun Preview() {
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    val modelProducer = CartesianChartModelProducer()
    runBlocking {
      modelProducer.runTransaction(
        SampleData.days,
        showProduction = true,
        showConsumption = true,
        showStorage = true,
        showGrid = true,
      )
    }
    MonthChart(modelProducer)
  }
}
