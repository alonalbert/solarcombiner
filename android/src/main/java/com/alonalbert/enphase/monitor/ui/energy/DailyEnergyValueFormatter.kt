package com.alonalbert.enphase.monitor.ui.energy

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.TabStopSpan
import androidx.compose.ui.graphics.Color
import androidx.core.text.buildSpannedString
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.ui.theme.colorOf
import com.alonalbert.enphase.monitor.ui.theme.toInt
import com.alonalbert.solar.combiner.enphase.util.kw
import com.alonalbert.solar.combiner.enphase.util.zerofy
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker.ValueFormatter

class DailyEnergyValueFormatter(
  private val androidContext: Context,
) : ValueFormatter {
  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>
  ): CharSequence {
    with(androidContext) {
      val solarColor = colorOf(R.color.solar)
      val gridColor = colorOf(R.color.grid)
      val batteryColor = colorOf(R.color.battery)
      val consumptionColor = colorOf(R.color.consumption)
      return buildSpannedString {
        targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>().forEach { target ->
          val columns = target.columns
          val produced = columns[0].entry.y
          val consumed = -columns[1].entry.y
          val grid = columns[2].entry.y
          val battery = columns[3].entry.y
          appendEnergyColumn("Produced", produced, solarColor)
          when (grid >= 0) {
            true -> appendEnergyColumn("Imported", grid, gridColor)
            false -> appendEnergyColumn("Exported", -grid, gridColor)
          }
          when (battery >= 0) {
            true -> appendEnergyColumn("Discharged", battery, batteryColor)
            false -> appendEnergyColumn("Charged", -battery, batteryColor)
          }
          appendEnergyColumn("Consumed", consumed, consumptionColor)

          setSpan(TabStopSpan.Standard(100), 0, length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }
  }
}

private fun SpannableStringBuilder.appendEnergyColumn(name: String, value: Double, color: Color) {
  if (value.zerofy() != 0.0) {
    append("$name:\t${value.kw}\n", ForegroundColorSpan(color.toInt()), SPAN_EXCLUSIVE_EXCLUSIVE)
  }
}
