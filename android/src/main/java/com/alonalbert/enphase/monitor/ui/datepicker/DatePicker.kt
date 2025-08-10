package com.alonalbert.enphase.monitor.ui.datepicker

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.enphase.util.formatMedium
import com.alonalbert.enphase.monitor.enphase.util.toEpochMillis
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset.UTC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPicker(
  date: LocalDate,
  onDayChanged: (LocalDate) -> Unit,
  installDate: LocalDate = LocalDate.of(2022, 4, 8),
) {
  val color = MaterialTheme.colorScheme.onBackground
  val disabledColor = color.copy(alpha = .5f)

  val day = date.atStartOfDay().toLocalDate()
  var showDatePickerDialog by remember { mutableStateOf(false) }

  if (showDatePickerDialog) {
    DatePickerDialog(day, installDate, { onDayChanged(it) }, { showDatePickerDialog = false })
  }
  Row(verticalAlignment = CenterVertically) {
    val prevEnabled = day > installDate
    IconButton(
      onClick = { onDayChanged(day.minusDays(1)) },
      enabled = prevEnabled,
    ) {
      Icon(
        Icons.Filled.ChevronLeft,
        "Previous",
        tint = if (prevEnabled) color else disabledColor,
        modifier = Modifier.size(40.dp)
      )
    }
    TextButton(
      onClick = { showDatePickerDialog = true },
      colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Filled.CalendarToday,
          contentDescription = "Select Date"
        )
        Spacer(modifier = Modifier.width(8.dp)) // Spacing between icon and text
        Text(
          text = day.formatMedium()
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
        tint = if (nextEnabled) color else disabledColor,
        modifier = Modifier.size(40.dp)
      )
    }


    if (nextEnabled) {
      Spacer(Modifier.width(8.dp))
      Button(
        onClick = { onDayChanged(LocalDate.now().atStartOfDay().toLocalDate()) },
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 32.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colorResource(R.color.solar),
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.heightIn(min = 32.dp)
      ) {
        Text(
          text = "Today",
          fontSize = 10.sp,
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DatePickerDialog(
  initialDate: LocalDate,
  installDate: LocalDate,
  onDayPicked: (LocalDate) -> Unit,
  onDialogClosed: () -> Unit,
) {
  val now = System.currentTimeMillis()
  val installMillis = installDate.toEpochSecond(LocalTime.MIN, UTC) * 1_000
  val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = initialDate.toEpochMillis(),
    selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in (installMillis..now)

      override fun isSelectableYear(year: Int) = year in installDate.year..LocalDate.now().year
    }
  )
  DatePickerDialog(
    onDismissRequest = onDialogClosed,
    confirmButton = {
      TextButton(
        onClick = {
          datePickerState.selectedDateMillis?.let {
            onDayPicked(LocalDate.ofInstant(Instant.ofEpochMilli(it), UTC))
          }
          onDialogClosed()
        }
      ) {
        Text("OK")
      }
    },
    dismissButton = {
      TextButton(onClick = onDialogClosed) {
        Text("Cancel")
      }
    }
  ) {
    DatePicker(state = datePickerState)
  }
}

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
  DayPicker(LocalDate.of(2022, 4, 8), {})
}

