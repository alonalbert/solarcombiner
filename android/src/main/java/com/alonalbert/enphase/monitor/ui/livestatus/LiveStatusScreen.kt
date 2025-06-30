package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.ui.components.EnergyArrow
import com.alonalbert.enphase.monitor.ui.theme.Colors
import com.alonalbert.enphase.monitor.util.toDisplay
import com.alonalbert.solar.combiner.enphase.calculateEnergyFlow
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import timber.log.Timber
import kotlin.math.abs

private val nodeRadius = 20.dp
private val nodeSize = 100.dp
private val nodeStroke = 2.dp
private val storagePad = 8.dp
private val gridPad = 32.dp
private val loadPad = gridPad

@Composable
fun LiveStatusScreen(
  modifier: Modifier = Modifier,
) {
  val viewModel: LiveStatusViewModel = hiltViewModel()
  val liveStatus by viewModel.liveStatusFlow.collectAsStateWithLifecycle()
  LiveStatusScreen(liveStatus, modifier)
}

@Composable
fun LiveStatusScreen(
  liveStatus: LiveStatus,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize(),
  ) { innerPadding ->
    Box(
      modifier = modifier
        .padding(innerPadding)
        .fillMaxSize()
        .aspectRatio(1f)
    ) {
      var pv by remember { mutableDoubleStateOf(0.0) }
      var storage by remember { mutableDoubleStateOf(0.0) }
      var grid by remember { mutableDoubleStateOf(0.0) }
      var load by remember { mutableDoubleStateOf(0.0) }
      val energyFlow = try {
        liveStatus.calculateEnergyFlow()
      } catch (e: IllegalArgumentException) {
        Timber.e(e)
        return@Box
      }
      var pvToLoad by remember { mutableDoubleStateOf(0.0) }
      var pvToStorage by remember { mutableDoubleStateOf(0.0) }
      var pvToGrid by remember { mutableDoubleStateOf(0.0) }
      var storageToLoad by remember { mutableDoubleStateOf(0.0) }
      var storageToGrid by remember { mutableDoubleStateOf(0.0) }
      var gridToLoad by remember { mutableDoubleStateOf(0.0) }
      var gridToStorage by remember { mutableDoubleStateOf(0.0) }

      pv = liveStatus.pv
      storage = liveStatus.storage
      grid = liveStatus.grid
      load = liveStatus.load
      pvToLoad = energyFlow.pvToLoad
      pvToStorage = energyFlow.pvToStorage
      pvToGrid = energyFlow.pvToGrid
      storageToLoad = energyFlow.storageToLoad
      storageToGrid = energyFlow.storageToGrid
      gridToLoad = energyFlow.gridToLoad
      gridToStorage = energyFlow.gridToStorage

      Node("Producing", pv, Colors.Produced, Alignment.TopCenter)
      Node("Consuming", load, Colors.Consumed, Alignment.CenterEnd, Modifier.padding(top = loadPad))
      Node("Discharging", storage, Colors.Battery, Alignment.BottomCenter, alternateName = "Charging")
      Node("Importing", grid, Colors.Grid, Alignment.CenterStart, Modifier.padding(top = gridPad), alternateName = "Exporting")

      val modifier = Modifier
        .fillMaxSize()
        .padding(nodeSize, nodeSize, nodeSize, bottom = nodeSize + storagePad)
      if (pvToLoad > 0) {
        PvToLoad(modifier)
      }
      if (pvToStorage > 0) {
        PvToStorage(modifier)
      }
      if (pvToGrid > 0) {
        PvToGrid(modifier)
      }
      if (storageToLoad > 0) {
        StorageToLoad(modifier)
      }
      if (storageToGrid > 0) {
        StorageToGrid(modifier)
      }
      if (gridToLoad > 0) {
        GridToLoad(modifier)
      }
      if (gridToStorage > 0) {
        GridToStorage(modifier)
      }
    }
  }
}

@Composable
private fun PvToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.CenterEnd,
    color = Colors.Produced,
    modifier = modifier,
  )
}

@Composable
private fun PvToGrid(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.CenterStart,
    color = Colors.Produced,
    modifier = modifier,
  )
}

@Composable
private fun StorageToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.BottomCenter,
    end = Alignment.CenterEnd,
    color = Colors.Battery,
    modifier = modifier,
  )
}

@Composable
private fun StorageToGrid(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.BottomCenter,
    end = Alignment.CenterStart,
    color = Colors.Battery,
    modifier = modifier,
  )
}

@Composable
private fun PvToStorage(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.BottomCenter,
    color = Colors.Produced,
    modifier = modifier,
  )
}

@Composable
private fun GridToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.CenterStart,
    end = Alignment.CenterEnd,
    color = Colors.Grid,
    modifier = modifier,
  )
}

@Composable
private fun GridToStorage(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.CenterStart,
    end = Alignment.BottomCenter,
    color = Colors.Grid,
    modifier = modifier,
  )
}

@Composable
private fun BoxScope.Node(
  name: String,
  value: Double,
  color: Color,
  alignment: Alignment,
  modifier: Modifier = Modifier,
  alternateName: String? = null
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .size(nodeSize)
      .align(alignment)
  ) {
    Canvas(modifier = Modifier.size(nodeRadius * 2)) {
      drawCircle(color = color, style = Stroke(nodeStroke.toPx()))
    }
    val label = when {
      value == 0.0 -> "Idle"
      alternateName == null -> name
      value < 0 -> alternateName
      else -> name
    }
    Text(abs(value).toDisplay("kW"), color = color)
    Text(label, color = Color.Gray, fontSize = 14.sp)
  }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800, backgroundColor = 0xFFE0E0E0)
@Composable
fun LiveStatusScreenPreview() {
  LiveStatusScreen(LiveStatus(pv = 10.2, storage = 0.6, grid = -3.84, load = 6.96))
}
