package com.alonalbert.enphase.monitor.ui.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.ui.theme.Colors
import com.alonalbert.solarsim.simulator.DailyEnergy
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.DecimalFormat

private val YDecimalFormat = DecimalFormat("#")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

private val RangeProvider =
  object : CartesianLayerRangeProvider {
    override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore) =
      0.0

    override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore) =
      96.0
  }

@Composable
fun DailyEnergyChart(
  dailyEnergy: DailyEnergy,
//  model: CartesianChartModel,
  modifier: Modifier = Modifier,
) {

  val x = (0 until 96).toList()
  val model = CartesianChartModel(ColumnCartesianLayerModel.build {
    series(x, dailyEnergy.energies.map { it.innerProduced + it.outerProduced })
    series(x, dailyEnergy.energies.map { -it.consumed })
    series(x, dailyEnergy.energies.map { it.imported - it.innerExported - it.outerProduced })
    series(x, dailyEnergy.energies.map { it.discharged - it.charged })
  })

  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          rangeProvider = RangeProvider,
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(fill = fill(Colors.Produced), thickness = 2.2.dp),
              rememberLineComponent(fill = fill(Colors.Consumed), thickness = 2.2.dp),
              rememberLineComponent(fill = fill(Colors.Imported), thickness = 2.2.dp),
              rememberLineComponent(fill = fill(Colors.Battery), thickness = 2.2.dp),
            ),
          columnCollectionSpacing = 0.8.dp,

          mergeMode = { ColumnCartesianLayer.MergeMode.stacked() },
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,

            valueFormatter = StartAxisValueFormatter,
          ),
        marker = rememberMarker(MarkerValueFormatter),
        layerPadding = { cartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp) },
      ),
    model = model,
    modifier = modifier.height(252.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
fun DailyEnergyChart(modifier: Modifier = Modifier) {
  DailyEnergyChart(SampleData.sampleData, modifier)
}

@Composable
@Preview
private fun Preview() {
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    DailyEnergyChart(SampleData.sampleData)
  }
}
