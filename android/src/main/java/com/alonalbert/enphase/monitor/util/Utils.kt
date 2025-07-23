package com.alonalbert.enphase.monitor.util

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.alonalbert.solar.combiner.enphase.util.round2

fun Double.toDisplay(
  unit: String,
  valueSize: TextUnit = 16.sp,
  valueWeight: FontWeight = FontWeight.Normal,
  unitSize: TextUnit = 12.sp,
  unitWeight: FontWeight = FontWeight.Normal,
) =
  buildAnnotatedString {
    withStyle(SpanStyle(fontSize = valueSize, fontWeight = valueWeight)) {
      append(this@toDisplay.round2)
    }
    withStyle(SpanStyle(fontSize = unitSize, fontWeight = unitWeight)) {
      append(" $unit")
    }
  }

context(drawScope: DrawScope)
val Dp.px get() =  with(drawScope) { toPx() }
