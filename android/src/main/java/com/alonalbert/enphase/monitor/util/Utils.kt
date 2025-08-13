package com.alonalbert.enphase.monitor.util

import android.content.res.Resources
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.enphase.util.kw
import com.alonalbert.enphase.monitor.enphase.util.round2
import com.alonalbert.enphase.monitor.enphase.util.zerofy
import com.alonalbert.enphase.monitor.ui.theme.toInt
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel

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
val Dp.px get() = with(drawScope) { toPx() }

@Composable
@ReadOnlyComposable
fun stringResourceOrDefault(@StringRes id: Int, default: String): String {
  val resources = LocalContext.current.resources
  return try {
    resources.getString(id)
  } catch (_: Resources.NotFoundException) {
    default
  }
}

context(scope: ColumnCartesianLayerModel.BuilderScope)
fun <T> List<T>.seriesOrEmpty(show: Boolean, transform: (T) -> Double) {
  with(scope) {
    val values = when (show) {
      true -> map(transform)
      false -> List(size) { 0.0 }
    }
    series(values)
  }
}

fun SpannableStringBuilder.appendEnergyValue(name: String, value: Double, color: Color) {
  if (value.zerofy() != 0.0) {
    append("$name:\t${value.kw}\n", ForegroundColorSpan(color.toInt()), SPAN_EXCLUSIVE_EXCLUSIVE)
  }
}
