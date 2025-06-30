package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.ui.components.EnergyArrow
import com.alonalbert.enphase.monitor.ui.theme.Colors
import com.alonalbert.enphase.monitor.util.toDisplay

private val nodeRadius = 20.dp
private val nodeSize = 80.dp
private val nodeStroke = 2.dp
private val storagePad = 8.dp
private val gridPad = 32.dp
private val loadPad = gridPad
private val offCenter = 8.dp
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
    Node("Consuming", 3.0, Colors.Consumed, Alignment.CenterEnd, Modifier.padding(top = loadPad))
    Node("Charging", 1.5, Colors.Battery, Alignment.BottomCenter)
    Node("Idle", 0.0, Colors.Grid, Alignment.CenterStart, Modifier.padding(top = gridPad))

    val modifier = Modifier.fillMaxSize().padding(nodeSize, nodeSize, nodeSize, bottom = nodeSize + storagePad)

    PvToStorage(modifier)
    GridToLoad(modifier)
    GridToStorage(modifier)
    PvToLoad(modifier)
    PvToGrid(modifier)
    StorageToLoad(modifier)
    StorageToGrid(modifier)
  }
}

@Composable
private fun PvToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.CenterEnd,
    color =  Colors.Produced,
    modifier = modifier,
  )
}

@Composable
private fun PvToGrid(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.CenterStart,
    color =  Colors.Produced,
    modifier = modifier,
  )
}

@Composable
private fun StorageToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.BottomCenter,
    end = Alignment.CenterEnd,
    color =  Colors.Battery,
    modifier = modifier,
  )
}

@Composable
private fun StorageToGrid(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.BottomCenter,
    end = Alignment.CenterStart,
    color =  Colors.Battery,
    modifier = modifier,
  )
}

@Composable
private fun PvToStorage(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.BottomCenter,
    color =  Colors.Produced,
    modifier = modifier,
    )
}

@Composable
private fun GridToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.CenterStart,
    end = Alignment.CenterEnd,
    color =  Colors.Grid,
    modifier = modifier,
    )
}

@Composable
private fun GridToStorage(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.CenterStart,
    end = Alignment.BottomCenter,
    color =  Colors.Grid,
    modifier = modifier,
    )
}

@Composable
private fun BoxScope.Node(name: String, value: Double, color: Color, alignment: Alignment, modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .size(nodeSize)
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
