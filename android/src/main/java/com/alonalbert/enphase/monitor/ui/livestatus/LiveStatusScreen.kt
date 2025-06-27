package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.ui.components.buildPath
import com.alonalbert.enphase.monitor.ui.components.drawLineWithArrow
import com.alonalbert.enphase.monitor.ui.theme.Colors
import com.alonalbert.enphase.monitor.util.toDisplay

private val nodeRadius = 20.dp
private val nodeSize = 80.dp
private val nodeStroke = 2.dp
private val storageOffset = 8.dp

private val arrowOffset = storageOffset
private val arrowRadius = 20.dp

@Composable
fun LiveStatusScreen(
  modifier: Modifier = Modifier,
) {
//  val nodeRadius = dimensionResource(R.dimen.node_radius)
  Box(modifier = modifier
    .fillMaxSize()
    .aspectRatio(1f)) {
    Node("Producing", 1.5, Colors.Produced, Alignment.TopCenter)
    Node("Consuming", 3.0, Colors.Consumed, Alignment.CenterEnd)
    Node("Charging", 1.5, Colors.Battery, Alignment.BottomCenter)
    Node("Idle", 0.0, Colors.Grid, Alignment.CenterStart)

    PvToStorage()
    GridToLoad()
    PvToLoad()
    PvToGrid()
    StorageToLoad()
  }
}

@Composable
private fun PvToLoad() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val arrowOffsetPx = arrowOffset.toPx()
    val arrowRadiusPx = arrowRadius.toPx()
    val x = center.x + arrowOffsetPx
    val path = buildPath {
      moveTo(x, nodeSize.toPx())
      val y1 = middle.y - arrowRadiusPx - arrowOffsetPx
      lineTo(x, y1)
      arcTo(Rect(Offset(center.x + arrowRadiusPx + arrowOffsetPx, y1), arrowRadiusPx), 180f, -90f, true)
    }
    val color = Colors.Produced
    drawPath(path, color = color, style = Stroke(1.dp.toPx()))
    val y2 = middle.y - arrowOffsetPx
    drawLineWithArrow(
      start = Offset(x + arrowRadiusPx, y2),
      end = Offset(size.width - nodeSize.toPx(), y2),
      color = color,
      strokeWidth = 1.dp.toPx(),
    )
  }
}

@Composable
private fun PvToGrid() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val arrowOffsetPx = arrowOffset.toPx()
    val arrowRadiusPx = arrowRadius.toPx()
    val x = center.x - arrowOffsetPx
    val path = buildPath {
      moveTo(x, nodeSize.toPx())
      val y1 = middle.y - arrowRadiusPx - arrowOffsetPx
      lineTo(x, y1)
      arcTo(Rect(Offset(center.x - arrowRadiusPx - arrowOffsetPx, y1), arrowRadiusPx), 0f, 90f, true)
    }
    val color = Colors.Produced
    drawPath(path, color = color, style = Stroke(1.dp.toPx()))
    val y2 = middle.y - arrowOffsetPx
    drawLineWithArrow(
      start = Offset(x - arrowRadiusPx, y2),
      end = Offset(nodeSize.toPx(), y2),
      color = color,
      strokeWidth = 1.dp.toPx(),
    )
  }
}

@Composable
private fun StorageToLoad() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val arrowOffsetPx = arrowOffset.toPx()
    val arrowRadiusPx = arrowRadius.toPx()
    val x = center.x + arrowOffsetPx
    val path = buildPath {
      moveTo(x, size.height - nodeSize.toPx() - storageOffset.toPx())
      val y1 = middle.y + arrowRadiusPx + arrowOffsetPx
      lineTo(x, y1)
      arcTo(Rect(Offset(center.x + arrowRadiusPx + arrowOffsetPx, y1), arrowRadiusPx), -90f, -90f, true)
    }
    val color = Colors.Battery
    drawPath(path, color = color, style = Stroke(1.dp.toPx()))
    val y2 = middle.y + arrowOffsetPx
    drawLineWithArrow(
      start = Offset(x + arrowRadiusPx, y2),
      end = Offset(size.width - nodeSize.toPx(), y2),
      color = color,
      strokeWidth = 1.dp.toPx(),
    )
  }
}

@Composable
private fun PvToStorage() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    drawLineWithArrow(
      start = Offset(center.x, nodeSize.toPx()),
      end = Offset(center.x, size.height - nodeSize.toPx() - storageOffset.toPx()),
      color = Colors.Produced,
      strokeWidth = 1.dp.toPx(),
      )
  }
}

@Composable
private fun GridToLoad() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    drawLineWithArrow(
      start = Offset(nodeSize.toPx(), middle.y),
      end = Offset(size.width - nodeSize.toPx(), middle.y),
      color = Colors.Grid,
      strokeWidth = 1.dp.toPx(),
      )
  }
}

@Composable
private fun BoxScope.Node(name: String, value: Double, color: Color, alignment: Alignment) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .size(nodeSize)
//      .offset(x, y)
      .align(alignment)
  ) {
    Canvas(modifier = Modifier.size(nodeRadius * 2)) {
      drawCircle(color = color, style = Stroke(nodeStroke.toPx()))
    }

    Text(value.toDisplay("kW"), color = color)
    Text(name, color = Color.Gray, fontSize = 14.sp)
  }
}

private val DrawScope.middle get() = Offset(center.x, center.y - nodeRadius.toPx())

@Preview(showBackground = true, widthDp = 400, heightDp = 800, backgroundColor = 0xFFE0E0E0)
@Composable
fun LiveStatusScreenPreview() {
  LiveStatusScreen(
  )
}
