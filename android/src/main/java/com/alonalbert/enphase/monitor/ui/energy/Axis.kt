package com.alonalbert.enphase.monitor.ui.energy

import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import java.text.DecimalFormat

val DecimalValueFormatter = CartesianValueFormatter.decimal(DecimalFormat("#.#"))

fun timeOfDayAxisValueFormatter(pointsPerHour: Int) = CartesianValueFormatter { _, x, _ ->
  when (val h = x.toInt() / pointsPerHour) {
    0, 24 -> "12am"
    12 -> "12pm"
    else -> (h % 12).toString()
  }
}
