package com.alonalbert.enphase.monitor.ui.datepicker

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alonalbert.enphase.monitor.ui.theme.Colors.Grey55
import com.alonalbert.solar.combiner.enphase.util.format
import java.time.LocalDate

val color = Grey55
val disabledColor = Grey55.copy(alpha = 0.4f)

@Composable
fun DayPicker(
  date: LocalDate,
  onDayChanged: (LocalDate) -> Unit,
  installDate: LocalDate = LocalDate.of(2025, 6, 17),
) {
  val day = date.atStartOfDay().toLocalDate()
  Row {
    val prevEnabled = day > installDate
    IconButton(
      onClick = { onDayChanged(day.minusDays(1)) },
      enabled = prevEnabled,
//      modifier = Modifier.padding(4.dp)
    ) {
      Icon(
        Icons.Filled.ChevronLeft,
        "Previous",
        tint = prevEnabled.toIconColor(),
        modifier = Modifier.size(40.dp)
      )
    }
    TextButton(
      onClick = { },
      colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.CalendarToday,
          contentDescription = "Select Date"
        )
        Spacer(modifier = Modifier.width(8.dp)) // Spacing between icon and text
        Text(
          text = day.format()
        )
      }
    }
    val nextEnabled = day < LocalDate.now()
    IconButton(
      { onDayChanged(day.plusDays(1)) },
      enabled = nextEnabled,
    ) {
      Icon(
        Icons.Filled.ChevronRight,
        "Next",
        tint = nextEnabled.toIconColor(),
        modifier = Modifier.size(40.dp)
      )
    }
  }
}

private fun Boolean.toIconColor() = if (this) color else disabledColor

@Preview(name = "Today")
@Composable
fun DayPickerPreview_Today() {
  DayPicker(LocalDate.now(), {})
}

@Preview(name = "Yesterday")
@Composable
fun DayPickerPreview_Yesterday() {
  DayPicker(LocalDate.now().minusDays(1), {})
}

@Preview(name = "First")
@Composable
fun DayPickerPreview_First() {
  DayPicker(LocalDate.of(2025, 6, 17), {})
}

