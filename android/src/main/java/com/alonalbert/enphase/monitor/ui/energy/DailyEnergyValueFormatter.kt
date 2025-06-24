package com.alonalbert.enphase.monitor.ui.energy

import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.TabStopSpan
import androidx.compose.ui.graphics.Color
import androidx.core.text.buildSpannedString
import com.alonalbert.enphase.monitor.ui.theme.Colors
import com.alonalbert.enphase.monitor.ui.theme.toInt
import com.alonalbert.solar.combiner.enphase.util.kw
import com.alonalbert.solar.combiner.enphase.util.zerofy
import com.alonalbert.solarsim.simulator.DailyEnergy
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker.ValueFormatter

class DailyEnergyValueFormatter(private val dailyEnergy: DailyEnergy):  ValueFormatter{
  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>
  ): CharSequence {
    val energies = dailyEnergy.energies
    return buildSpannedString {
      targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>().forEach { target ->
        val columns = target.columns
        val i = columns[0].entry.x.toInt()
        val energy = energies[i]
        appendEnergyColumn("Produced", energy.outerProduced + energy.innerProduced, Colors.Produced)
        val imported = energy.imported - energy.innerExported - energy.outerProduced
        if (imported >= 0) {
          appendEnergyColumn("Imported", imported, Colors.Imported)
        } else {
          appendEnergyColumn("Exported", -imported, Colors.Imported)
        }
        appendEnergyColumn("Charged", energy.charged, Colors.Battery)
        appendEnergyColumn("Discharged", energy.discharged, Colors.Battery)
        appendEnergyColumn("Consumed", energy.consumed, Colors.Consumed)

        setSpan(TabStopSpan.Standard(100), 0, length, SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }
}

private fun SpannableStringBuilder.appendEnergyColumn(name: String, value: Double, color: Color) {
  if (value.zerofy() != 0.0) {
    append("$name:\t${value.kw}\n", ForegroundColorSpan(color.toInt()), SPAN_EXCLUSIVE_EXCLUSIVE)
  }
}
