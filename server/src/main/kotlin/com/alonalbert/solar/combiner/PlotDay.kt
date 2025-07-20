package com.alonalbert.solar.combiner

import com.alonalbert.solar.combiner.enphase.EnergyColors
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.gather
import org.jetbrains.kotlinx.dataframe.api.into
import org.jetbrains.kotlinx.kandy.dsl.categorical
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.ir.Plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.Position
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.feature.position
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.scales.guide.LegendType
import org.jetbrains.kotlinx.kandy.letsplot.scales.guide.model.AxisPosition
import org.jetbrains.kotlinx.kandy.letsplot.settings.LineType
import org.jetbrains.kotlinx.kandy.letsplot.style.LegendPosition
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import java.nio.file.Path
import kotlin.io.path.createDirectories

private val COLOR_PRODUCED = Color.hex("#%06X".format(EnergyColors.produced))
private val COLOR_CONSUMED = Color.hex("#%06X".format(EnergyColors.consumed))
private val COLOR_IMPORTED = Color.hex("#%06X".format(EnergyColors.imported))
private val COLOR_BATTERY = Color.hex("#%06X".format(EnergyColors.battery))

fun DailyEnergy.plotEnergy(filename: String, batteryCapacity: Double? = null) {
  Path.of(filename).parent.createDirectories()
  plotEnergy(batteryCapacity).save(filename, path = ".")
}

fun DailyEnergy.plotEnergy(batteryCapacity: Double? = null): Plot {
  val max = energies.maxOf { it.imported + it.exportProduced + it.mainProduced } * 1.1
  val dataFrame = dataFrameOf(
    "time" to List(energies.size) { it.toFloat() / 4 },
    "produced" to energies.map { it.mainProduced + it.exportProduced },
    "consumed" to energies.map { -it.consumed },
    "imported" to energies.map { it.imported - it.mainExported - it.exportProduced },
    "charged" to energies.map { it.discharged - it.charged },
    "battery" to energies.map { it.battery?.toDouble()?.div(100)?.times(max - 0.5) },
  ).gather(
    "produced",
    "consumed",
    "imported",
    "charged",
  ).into("name", "value")

  return dataFrame.plot {
    x("time")
    x {
      limits = 0..24
    }
    y {
      axis.position = AxisPosition.BOTH
      limits = -max..max
    }
    bars {
      y("value")
      fillColor("name") {
        scale = categorical(
          "produced" to COLOR_PRODUCED,
          "consumed" to COLOR_CONSUMED,
          "imported" to COLOR_IMPORTED,
          "charged" to COLOR_BATTERY,
        )
        legend.breaksLabeled(
          "produced" to """
            Produced: %.2f
          """.trimIndent().format(mainProduced + exportProduced),
          "consumed" to "Consumed: %.2f".format(consumed),
          "imported" to """
            Imported: %.2f
            Exported: %.2f
            Net import: %.2f
          """.trimIndent().format(imported, mainExported + exportProduced, imported - mainExported - exportProduced),
          "charged" to """
            Charged: %.2f
            Discharged: %.2f
          """.trimIndent().format(charged, discharged),
        )
        legend.type = LegendType.DiscreteLegend(5)
      }
      position = Position.stack()
    }
    if (batteryCapacity != null) {
      line {
        y("battery") {
          color = COLOR_BATTERY
          type = LineType.DASHED
          width = 1.5
        }
      }
    }
    layout {
      title = "Energy"
      size = 1000 to 700
      style {
        axis.title { blank = true }
        legend.position = LegendPosition.Bottom
        legend.title {
          blank = true
        }
        legend.text {
          fontSize = 22.0
        }
      }
    }
  }
}

//fun DailyEnergy.plotBattery(batteryCapacity: Double): Plot {
//  val dataFrame = dataFrameOf(
//    "time" to List(energies.size) { it.toFloat() / 4 },
//    "battery" to energies.map { it.battery * 100 / batteryCapacity },
//  )
//  return dataFrame.plot {
//    x("time")
//    x {
//      limits = 0..24
//    }
//    y {
//      axis.position = AxisPosition.BOTH
//      limits = 0..100
//    }
//    line {
//      y("battery") {
//        color = COLOR_BATTERY
//        type = LineType.DASHED
//        width = 1.5
//      }
//    }
//    layout {
//      title = "Battery"
//      size = 1000 to 700
//      style {
//        axis.title { blank = true }
//        legend.position = LegendPosition.Bottom
//        legend.title {
//          blank = true
//        }
//        legend.text {
//          fontSize = 22.0
//        }
//      }
//    }
//  }
//}