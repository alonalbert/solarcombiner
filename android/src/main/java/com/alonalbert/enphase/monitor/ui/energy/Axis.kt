package com.alonalbert.enphase.monitor.ui.energy

import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import java.text.DecimalFormat

val DecimalValueFormatter = CartesianValueFormatter.decimal(DecimalFormat("#"))

val TimeOfDayAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
  when (val h = x.toInt() / 4) {
    0, 24 -> "12am"
    12 -> "12pm"
    else -> (h % 12).toString()
  }
}
