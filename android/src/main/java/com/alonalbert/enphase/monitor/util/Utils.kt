package com.alonalbert.enphase.monitor.util

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.enphase.util.round2

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
