package com.alonalbert.enphase.monitor.ui.battery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.util.toDisplay

private val SOC_ICONS = listOf(
  R.drawable.soc_1,
  R.drawable.soc_2,
  R.drawable.soc_3,
  R.drawable.soc_4,
  R.drawable.soc_5,
)

@Composable
fun BatteryBar(soc: Int, capacity: Double, reserve: Int, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .wrapContentSize()
      .fillMaxWidth()
      .border(1.dp, Color.LightGray)
  ) {
    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.SpaceAround,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        Row(modifier = Modifier.padding(8.dp)) {
          val icon = (soc / (100 / SOC_ICONS.size)).coerceIn(0, SOC_ICONS.size)
          Image(
            painterResource(SOC_ICONS[icon]),
            contentDescription = null,
            modifier = Modifier
              .height(32.dp)
              .aspectRatio(1.0f)
          )
          Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("CHARGE", color = Color.Gray)
            val text = buildAnnotatedString {
              append("$soc%  - (")
              append(
                (capacity * soc / 100).toDisplay(
                  "kWh",
                  valueSize = 14.sp,
                  unitSize = 12.sp,
                )
              )
              append(")")
            }
            Text(text)
          }
        }
      }
      Box(
        modifier = Modifier
          .height(32.dp)
          .width(1.dp)
          .background(Color.LightGray)
      )
      Column {
        Row(verticalAlignment = CenterVertically, modifier = Modifier.padding(8.dp)) {
          Icon(
            imageVector = Icons.Filled.Settings,
            tint = Color.Gray,
            contentDescription = null,
          )
          Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("PROFILE", color = Color.Gray)
            Text("Self consumption: $reserve%")
          }
        }
      }
    }
  }
}

@Preview(widthDp = 400, heightDp = 100)
@Composable
fun BatteryBarPreview_10() {
  BatteryBar(10, 20.160, 88)
}
@Preview(widthDp = 400, heightDp = 100)
@Composable
fun BatteryBarPreview_20() {
  BatteryBar(20, 20.160, 88)
}
@Preview(widthDp = 400, heightDp = 100)
@Composable
fun BatteryBarPreview_40() {
  BatteryBar(40, 20.160, 88)
}
@Preview(widthDp = 400, heightDp = 100)
@Composable
fun BatteryBarPreview_60() {
  BatteryBar(60, 20.160, 88)
}
@Preview(widthDp = 400, heightDp = 100)
@Composable
fun BatteryBarPreview_80() {
  BatteryBar(80, 20.160, 88)
}
