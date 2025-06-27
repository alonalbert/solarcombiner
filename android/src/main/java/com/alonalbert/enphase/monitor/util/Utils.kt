package com.alonalbert.enphase.monitor.util

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.alonalbert.solar.combiner.enphase.util.round

fun Double.toDisplay(unit: String, valueSize: TextUnit = 16.sp, unitSize: TextUnit = 12.sp) =
  buildAnnotatedString {
    withStyle(SpanStyle(fontSize = valueSize)) {
      append(this@toDisplay.round)
    }
    withStyle(SpanStyle(fontSize = unitSize)) {
      append(" $unit")
    }
  }
