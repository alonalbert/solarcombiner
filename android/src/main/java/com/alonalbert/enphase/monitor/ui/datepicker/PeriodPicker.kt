package com.alonalbert.enphase.monitor.ui.datepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun PeriodPicker(
  period: Period,
  onPeriodChanged: (Period) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    PeriodButton(
      text = "Day",
      isSelected = period is DayPeriod,
      onClick = { onPeriodChanged(DayPeriod(LocalDate.now())) }
    )
    PeriodButton(
      text = "Month",
      isSelected = period is MonthPeriod,
      onClick = { onPeriodChanged(MonthPeriod(YearMonth.now())) }
    )
  }
}

@Composable
fun PeriodButton(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = CircleShape
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    )
  }
}

@Preview(name = "Today")
@Composable
private fun PeriodPickerPreview_Month() {
  PeriodPicker(MonthPeriod(YearMonth.now()), {})
}
