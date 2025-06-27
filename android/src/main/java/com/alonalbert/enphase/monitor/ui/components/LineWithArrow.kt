package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawLineWithArrow(
  start: Offset,
  end: Offset,
  color: Color = Color.Black,
  strokeWidth: Float = 2f,
  arrowheadLength: Float = 20f, // Length of the arrowhead lines
  arrowheadAngleDegrees: Float = 30f // Angle of the arrowhead lines relative to the main line
) {
  // Calculate the angle of the main line
  val angleRad = atan2(end.y - start.y, end.x - start.x)

  // Calculate points for the arrowhead
  // The arrowhead will be at the 'end' of the line

  // Point 1 of arrowhead
  val angle1 = angleRad - Math.toRadians(arrowheadAngleDegrees.toDouble())
  val x1 = end.x - arrowheadLength * cos(angle1).toFloat()
  val y1 = end.y - arrowheadLength * sin(angle1).toFloat()

  // Point 2 of arrowhead
  val angle2 = angleRad + Math.toRadians(arrowheadAngleDegrees.toDouble())
  val x2 = end.x - arrowheadLength * cos(angle2).toFloat()
  val y2 = end.y - arrowheadLength * sin(angle2).toFloat()

  // Draw the main line

  drawLine(
    color = color,
    start = start,
    end = Offset((x1 + x2) / 2, (y1 + y2) / 2 ),
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round // Optional: for rounded line ends
  )

  val arrowheadPath = buildPath {
    moveTo(end.x, end.y)
    lineTo(x1, y1)
    lineTo(x2, y2)
    close()
  }
  drawPath(
    path = arrowheadPath,
    color = color,
    style = Fill,
  )
}

@Composable
private fun LineWithArrow(
  start: Offset,
  end: Offset,
  color: Color = Color.Black,
  strokeWidth: Float = 2f,
  arrowheadLength: Float = 20f, // Length of the arrowhead lines
  arrowheadAngleDegrees: Float = 30f // Angle of the arrowhead lines relative to the main line
) {
  Canvas(modifier = Modifier.fillMaxSize()) {
    drawLineWithArrow(start, end, color, strokeWidth, arrowheadLength, arrowheadAngleDegrees)
  }
}

@Preview(widthDp = 150, heightDp = 150)
@Composable
fun LineWithArrowPreview() {
  LineWithArrow(
    start = Offset(100f, 100f),
    end = Offset(300f, 100f), // Horizontal line
    strokeWidth = 2f,
    arrowheadLength = 20f,
    arrowheadAngleDegrees = 25f
  )
}

@Preview(widthDp = 150, heightDp = 150)
@Composable
fun AngledLineWithArrowPreview() {
  LineWithArrow(
    start = Offset(100f, 300f),
    end = Offset(300f, 100f), // Angled line
    color = Color.Blue,
    strokeWidth = 8f,
    arrowheadLength = 30f
  )
}

@Preview(widthDp = 150, heightDp = 150)
@Composable
fun VerticalLineWithArrowPreview() {
  LineWithArrow(
//    modifier = Modifier.padding(20.dp), // Add padding to see the full arrow if near edge
    start = Offset(100f, 100f),
    end = Offset(100f, 300f), // Vertical line, arrow pointing down
    color = Color.Red
  )
}
