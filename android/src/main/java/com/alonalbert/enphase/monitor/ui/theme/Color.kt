package com.alonalbert.enphase.monitor.ui.theme

import androidx.compose.ui.graphics.Color
import com.alonalbert.solar.combiner.enphase.EnergyColors

object Colors {
  val Purple80 = Color(0xFFD0BCFF)
  val PurpleGrey80 = Color(0xFFCCC2DC)
  val Pink80 = Color(0xFFEFB8C8)

  val Purple40 = Color(0xFF6650a4)
  val PurpleGrey40 = Color(0xFF625b71)
  val Pink40 = Color(0xFF7D5260)

  val Grey55 = Color(0xFF555555)

  val Produced = Color(EnergyColors.produced).copy(alpha = 1.0f)
  val Consumed = Color(EnergyColors.consumed).copy(alpha = 1.0f)
  val Imported = Color(EnergyColors.imported).copy(alpha = 1.0f)
  val Battery = Color(EnergyColors.battery).copy(alpha = 1.0f)
}

