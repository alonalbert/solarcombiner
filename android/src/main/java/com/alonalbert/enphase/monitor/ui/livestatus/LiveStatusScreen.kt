package com.alonalbert.enphase.monitor.ui.livestatus

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.enphase.calculateEnergyFlow
import com.alonalbert.enphase.monitor.enphase.model.LiveStatus
import com.alonalbert.enphase.monitor.enphase.util.format
import com.alonalbert.enphase.monitor.enphase.util.round1
import com.alonalbert.enphase.monitor.enphase.util.zerofy
import com.alonalbert.enphase.monitor.ui.battery.BatteryBar
import com.alonalbert.enphase.monitor.ui.components.EnergyArrow
import com.alonalbert.enphase.monitor.util.toDisplay
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

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
  val capacity = 20.16
  Scaffold(
    modifier = Modifier.fillMaxSize(),
  ) { innerPadding ->
    Column(
      modifier = modifier
        .padding(innerPadding)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        contentAlignment = Alignment.Center, modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        BatteryBar(liveStatus.soc, capacity, liveStatus.reserve)
      }
      Box(
        modifier = modifier
          .fillMaxWidth()
          .aspectRatio(1f)
      ) {
        var pv by remember { mutableDoubleStateOf(0.0) }
        var exportPv by remember { mutableStateOf<Double?>(null) }
        var storage by remember { mutableDoubleStateOf(0.0) }
        var grid by remember { mutableDoubleStateOf(0.0) }
        var load by remember { mutableDoubleStateOf(0.0) }
        val energyFlow = liveStatus.calculateEnergyFlow()
        var pvToLoad by remember { mutableDoubleStateOf(0.0) }
        var pvToStorage by remember { mutableDoubleStateOf(0.0) }
        var pvToGrid by remember { mutableDoubleStateOf(0.0) }
        var storageToLoad by remember { mutableDoubleStateOf(0.0) }
        var storageToGrid by remember { mutableDoubleStateOf(0.0) }
        var gridToLoad by remember { mutableDoubleStateOf(0.0) }
        var gridToStorage by remember { mutableDoubleStateOf(0.0) }

        pv = liveStatus.pv
        exportPv = liveStatus.exportPv
        storage = liveStatus.storage
        grid = liveStatus.grid
        load = liveStatus.load
        pvToLoad = energyFlow.pvToLoad
        pvToStorage = energyFlow.pvToStorage.zerofy()
        pvToGrid = energyFlow.pvToGrid.zerofy()
        storageToLoad = energyFlow.storageToLoad.zerofy()
        storageToGrid = energyFlow.storageToGrid.zerofy()
        gridToLoad = energyFlow.gridToLoad.zerofy()
        gridToStorage = energyFlow.gridToStorage.zerofy()

        val producing = buildString {
          append("Producing")
          val exportPv = exportPv
          if (exportPv != null) {
            append(" (${pv.round1} + ${exportPv.round1})")
          }
        }

        Node(
          name = producing,
          iconRes = R.drawable.solar,
          value = pv + (exportPv ?: 0.0),
          color = colorResource(R.color.solar),
          alignment = Alignment.TopCenter,
          modifier = Modifier.height(nodeSize),
        )
        Node(
          name = "Consuming",
          iconRes = R.drawable.house,
          value = load,
          color = colorResource(R.color.consumption),
          alignment = Alignment.CenterEnd,
          modifier = Modifier
            .padding(top = loadPad)
            .size(nodeSize)
        )

        val batteryLevel = capacity * liveStatus.soc.toDouble() / 100
        val charging = buildString {
          append("Full by ")
          val rate = abs(gridToStorage + pvToStorage)
          if (rate > 0) {
            val remaining = ((capacity - batteryLevel) / rate).hours
            append(remaining.format())
          }
        }
        val discharging = buildString {
          val rate = abs(storageToGrid + storageToLoad)
          append("Empty by ")
          if (rate > 0) {
            val remaining = ((batteryLevel - capacity * 0.1) / rate).hours
            append(remaining.format())
          }
        }
        Node(
          name = discharging,
          iconRes = R.drawable.battery,
          value = storage,
          color = colorResource(R.color.battery),
          alignment = Alignment.BottomCenter,
          alternateName = charging,
          modifier = Modifier.height(nodeSize),
        )
        Node(
          name = "Importing",
          iconRes = R.drawable.grid,
          value = grid,
          color = colorResource(R.color.grid),
          alignment = Alignment.CenterStart,
          modifier = Modifier
            .padding(top = gridPad)
            .size(nodeSize),
          alternateName = "Exporting",
        )

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
}

@Composable
private fun PvToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.CenterEnd,
    color = colorResource(R.color.solar),
    modifier = modifier,
  )
}

@Composable
private fun PvToGrid(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.CenterStart,
    color = colorResource(R.color.solar),
    modifier = modifier,
  )
}

@Composable
private fun StorageToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.BottomCenter,
    end = Alignment.CenterEnd,
    color = colorResource(R.color.battery),
    modifier = modifier,
  )
}

@Composable
private fun StorageToGrid(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.BottomCenter,
    end = Alignment.CenterStart,
    color = colorResource(R.color.battery),
    modifier = modifier,
  )
}

@Composable
private fun PvToStorage(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.TopCenter,
    end = Alignment.BottomCenter,
    color = colorResource(R.color.solar),
    modifier = modifier,
  )
}

@Composable
private fun GridToLoad(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.CenterStart,
    end = Alignment.CenterEnd,
    color = colorResource(R.color.grid),
    modifier = modifier,
  )
}

@Composable
private fun GridToStorage(modifier: Modifier) {
  EnergyArrow(
    start = Alignment.CenterStart,
    end = Alignment.BottomCenter,
    color = colorResource(R.color.grid),
    modifier = modifier,
  )
}

@Composable
private fun BoxScope.Node(
  name: String,
  @DrawableRes iconRes: Int,
  value: Double,
  color: Color,
  alignment: Alignment,
  modifier: Modifier = Modifier,
  alternateName: String? = null
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .align(alignment)
  ) {
    Box(
      modifier = Modifier.size(nodeRadius * 2),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(color = color, style = Stroke(nodeStroke.toPx()))
      }
      Image(
        painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(0.7f),
        alignment = Alignment.Center,
      )
    }
    val label = when {
      value == 0.0 -> "Idle"
      alternateName == null -> name
      value < 0 -> alternateName
      else -> name
    }
    Text(abs(value).toDisplay("kW"), color = color)
    Text(label, color = color, fontSize = 14.sp)
  }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800, backgroundColor = 0xFFE0E0E0)
@Composable
fun LiveStatusScreenPreview() {
  LiveStatusScreen(LiveStatus(pv = 6.2, exportPv = 4.0, storage = 0.6, grid = -3.84, load = 6.96, soc = 20, reserve = 24))
}

private fun Duration.format(): String {
  return LocalDateTime.now().plusMinutes(inWholeMinutes).format()
}