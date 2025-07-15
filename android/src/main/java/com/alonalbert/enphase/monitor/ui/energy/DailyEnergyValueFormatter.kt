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
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import com.alonalbert.solar.combiner.enphase.util.kw
import com.alonalbert.solar.combiner.enphase.util.zerofy
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker.ValueFormatter

class DailyEnergyValueFormatter(
  private val androidContext: Context,
  private val dailyEnergy: DailyEnergy,
  ):  ValueFormatter{
  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>
  ): CharSequence {
    with (androidContext) {
      val solarColor = colorOf(R.color.solar)
      val gridColor = colorOf(R.color.grid)
      val batteryColor = colorOf(R.color.battery)
      val consumptionColor = colorOf(R.color.consumption)
      val energies = dailyEnergy.energies
      return buildSpannedString {
        targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>().forEach { target ->
          val columns = target.columns
          val i = columns[0].entry.x.toInt()
          val energy = energies[i]
          appendEnergyColumn("Produced", energy.outerProduced + energy.innerProduced, solarColor)
          val imported = energy.imported - energy.innerExported - energy.outerProduced
          if (imported >= 0) {
            appendEnergyColumn("Imported", imported, gridColor)
          } else {
            appendEnergyColumn("Exported", -imported, gridColor)
          }
          appendEnergyColumn("Charged", energy.charged, batteryColor)
          appendEnergyColumn("Discharged", energy.discharged, batteryColor)
          appendEnergyColumn("Consumed", energy.consumed, consumptionColor)

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
