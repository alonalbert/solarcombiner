package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.util.px
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin


fun DrawScope.energyArrow(
  start: Offset,
  corner: Offset,
  end: Offset,
  color: Color,
  radius: Dp,
  strokeWidth: Float = 1.dp.px,
  arrowLengthDp: Dp = 8.dp,
  arrowAngle: Float = 60f,
) {
  assert(
    (corner.x == start.x && corner.y == end.y) || (corner.x == end.x && corner.y == start.y)
  ) { "Corner $corner is invalid between points $start - $end" }

  val angle = atan2(end.y - corner.y, end.x - corner.x)

  val radiusPx = radius.px
  val signX1 = sign(corner.x - start.x)
  val signY1 = sign(corner.y - start.y)
  val signX2 = sign(end.x - corner.x)
  val signY2 = sign(end.y - corner.y)

  val line1End = Offset(corner.x - radiusPx * signX1, corner.y - radiusPx * signY1)
  val line2Start = Offset(corner.x + radiusPx * signX2, corner.y + radiusPx * signY2)

  val radians = Math.toRadians(arrowAngle.toDouble() / 2)
  val angle1 = angle - radians
  val arrowLengthPx = arrowLengthDp.px
  val arrow1 = Offset(end.x - arrowLengthPx * cos(angle1).toFloat(), end.y - arrowLengthPx * sin(angle1).toFloat())
  val angle2 = angle + radians
  val arrow2 = Offset(end.x - arrowLengthPx * cos(angle2).toFloat(), end.y - arrowLengthPx * sin(angle2).toFloat())

  val mx = line1End.x + line2Start.x - corner.x
  val my = line1End.y + line2Start.y - corner.y
  val rect = Rect(Offset(mx, my), radiusPx)

  val s = sign((end.x - start.x) * (end.y - start.y))
  val startAngle = if (start.x == corner.x) (signX2 + 1) * 90f else signY2 * -90f
  val sweepAngle = s * if (start.x == corner.x) -90f else 90f

  val linePath = buildPath {
    moveTo(start)
    lineTo(line1End)
    arcTo(rect, startAngle, sweepAngle, true)
    lineTo(line2Start)
    lineTo((arrow1.x + arrow2.x) / 2, (arrow1.y + arrow2.y) / 2)
  }
  drawPath(linePath, color, style = Stroke(strokeWidth))

  val arrowPath = buildPath {
    moveTo(end)
    lineTo(arrow1)
    lineTo(arrow2)
    close()
  }
  drawPath(arrowPath, color, style = Fill)
}

fun DrawScope.energyArrow(
  start: Offset,
  end: Offset,
  color: Color,
  strokeWidth: Float = 1.dp.px,
  arrowLengthDp: Dp = 8.dp,
  arrowAngle: Float = 60f,
) {
  val angle = atan2(end.y - start.y, end.x - start.x)

  val radians = Math.toRadians(arrowAngle.toDouble() / 2)
  val angle1 = angle - radians
  val arrowLengthPx = arrowLengthDp.px
  val arrow1 = Offset(end.x - arrowLengthPx * cos(angle1).toFloat(), end.y - arrowLengthPx * sin(angle1).toFloat())
  val angle2 = angle + radians
  val arrow2 = Offset(end.x - arrowLengthPx * cos(angle2).toFloat(), end.y - arrowLengthPx * sin(angle2).toFloat())

  val linePath = buildPath {
    moveTo(start)
    lineTo((arrow1.x + arrow2.x) / 2, (arrow1.y + arrow2.y) / 2)
  }
  drawPath(linePath, color, style = Stroke(strokeWidth))

  val arrowPath = buildPath {
    moveTo(end)
    lineTo(arrow1)
    lineTo(arrow2)
    close()
  }
  drawPath(arrowPath, color, style = Fill)

}

@Preview(widthDp = 400, heightDp = 400)
@Composable
fun EnergyArrow1() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    energyArrow(
      start = Offset(200.dp.px, 20.dp.px),
      end = Offset(200.dp.px, 380.dp.px),
      color = Color.Black,
    )

    energyArrow(
      start = Offset(20.dp.px, 200.dp.px),
      end = Offset(380.dp.px, 200.dp.px),
      color = Color.Black,
    )

    energyArrow(
      start = Offset(220.dp.px, 20.dp.px),
      corner = Offset(220.dp.px, 180.dp.px),
      end = Offset(380.dp.px, 180.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
    energyArrow(
      start = Offset(180.dp.px, 20.dp.px),
      corner = Offset(180.dp.px, 180.dp.px),
      end = Offset(20.dp.px, 180.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
    energyArrow(
      start = Offset(220.dp.px, 380.dp.px),
      corner = Offset(220.dp.px, 220.dp.px),
      end = Offset(380.dp.px, 220.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
    energyArrow(
      start = Offset(180.dp.px, 380.dp.px),
      corner = Offset(180.dp.px, 220.dp.px),
      end = Offset(20.dp.px, 220.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
  }
}

@Preview(widthDp = 400, heightDp = 400)
@Composable
fun EnergyArrow2() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    energyArrow(
      start = Offset(200.dp.px, 380.dp.px),
      end = Offset(200.dp.px, 20.dp.px),
      color = Color.Black,
    )
    energyArrow(
      start = Offset(380.dp.px, 200.dp.px),
      end = Offset(20.dp.px, 200.dp.px),
      color = Color.Black,
    )
    energyArrow(
      start = Offset(380.dp.px, 180.dp.px),
      corner = Offset(220.dp.px, 180.dp.px),
      end = Offset(220.dp.px, 20.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
    energyArrow(
      start = Offset(20.dp.px, 180.dp.px),
      corner = Offset(180.dp.px, 180.dp.px),
      end = Offset(180.dp.px, 20.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
    energyArrow(
      start = Offset(380.dp.px, 220.dp.px),
      corner = Offset(220.dp.px, 220.dp.px),
      end = Offset(220.dp.px, 380.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
    energyArrow(
      start = Offset(20.dp.px, 220.dp.px),
      corner = Offset(180.dp.px, 220.dp.px),
      end = Offset(180.dp.px, 380.dp.px),
      color = Color.Black,
      radius = 20.dp,
    )
  }
}

