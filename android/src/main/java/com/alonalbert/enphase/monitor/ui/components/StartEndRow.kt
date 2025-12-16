package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun StartEndRow(
  modifier: Modifier = Modifier,
  left: @Composable RowScope.() -> Unit,
  right: @Composable RowScope.() -> Unit,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    left()
    right()
  }
}

@Preview(showBackground = true)
@Composable
private fun Row1Preview() {
  StartEndRow(
    left = { Text("Left Content") },
    right = { Text("Right Content") }
  )
}