package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.util.px

@Composable
fun EnergyArrow(
  start: Alignment,
  end: Alignment,
  color: Color = Color.Black,
  pad: Dp = 8.dp,
  radius: Dp = 20.dp,
  strokeWidth: Dp = 1.dp,
  arrowLengthDp: Dp = 8.dp,
  arrowAngle: Float = 60f,
  pointPosition: Float? = null,
  modifier: Modifier = Modifier
) {
  assert(start.isValid())
  assert(end.isValid())
  assert(end != start)

  Canvas(modifier = modifier.fillMaxSize()) {
    val hasCorner = start.vBias() != end.vBias() && start.hBias() != end.hBias()
    val padPx = if (hasCorner) pad.px else 0f
    val startOffset = start.toOffset(padPx, end)
    val endOffset = end.toOffset(padPx, start)

    val linePath = buildPath {
      moveTo(startOffset)
      if (hasCorner) {
        addArc(start, end, padPx, radius.px)
      }
      lineTo(endOffset)
    }
    drawPath(linePath, color, style = Stroke(strokeWidth.px))
    drawCircle(Color.Red, 4.dp.px, startOffset)
  }
}

context(drawScope: DrawScope)
private fun Path.addArc(
  start: Alignment,
  end: Alignment,
  padPx: Float,
  radius: Float,
) {
  with(drawScope) {
    val dx = start.hBias() + end.hBias()
    val dy = start.vBias() + end.vBias()
    val middle = Offset(center.x + padPx * dx, center.y + padPx * dy)
    val rectCenter = Offset(middle.x + radius * dx, middle.y + radius * dy)
    val rect = Rect(rectCenter, radius)
    arcTo(rect, end.getStartAngle(), (start to end).getSweepAngle(), false)
  }
}

private fun Alignment.isValid() =
  this == TopCenter || this == BottomCenter || this == CenterStart || this == CenterEnd

private fun Alignment.hBias() = (this as BiasAlignment).horizontalBias
private fun Alignment.vBias() = (this as BiasAlignment).verticalBias

context(drawScope: DrawScope)
private fun Alignment.toOffset(pad: Float, other: Alignment) =
  with(drawScope) {
    Offset(
      center.x + hBias() * center.x + pad * other.hBias(),
      center.y + vBias() * center.y + pad * other.vBias(),
    )
  }

private fun Pair<Alignment, Alignment>.getSweepAngle() =
  (first.hBias() + first.vBias()) * (second.hBias() - second.vBias()) * 90f

private fun Alignment.getStartAngle() = (hBias() + 1) * (hBias() - vBias()) * 90f

@Preview(widthDp = 400, heightDp = 400)
@Composable
fun EnergyArrowPreview1() {
  EnergyArrow(
    start = TopCenter,
    end = BottomCenter,
  )
  EnergyArrow(
    start = CenterStart,
    end = CenterEnd,
  )
  EnergyArrow(
    start = TopCenter,
    end = CenterEnd,
  )
  EnergyArrow(
    start = TopCenter,
    end = CenterStart,
  )
  EnergyArrow(
    start = BottomCenter,
    end = CenterEnd,
  )
  EnergyArrow(
    start = BottomCenter,
    end = CenterStart,
  )
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
fun EnergyArrowPreview2() {
  EnergyArrow(
    start = BottomCenter,
    end = TopCenter,
  )
  EnergyArrow(
    start = CenterEnd,
    end = CenterStart,
  )
  EnergyArrow(
    start = CenterEnd,
    end = TopCenter,
  )
  EnergyArrow(
    start = CenterStart,
    end = TopCenter,
  )
  EnergyArrow(
    start = CenterEnd,
    end = BottomCenter,
  )
  EnergyArrow(
    start = CenterStart,
    end = BottomCenter,
  )
}
