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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.alonalbert.enphase.monitor.enphase.util.format
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthPicker(
  month: YearMonth,
  onMonthChanged: (YearMonth) -> Unit,
  installDate: LocalDate = INSTALL_DATE,
) {
  val color = MaterialTheme.colorScheme.onBackground
  val disabledColor = color.copy(alpha = .5f)


  var showMonthPickerDialog by remember { mutableStateOf(false) }

  if (showMonthPickerDialog) {
    TODO()
//    MonthPickerDialog(month, installDate, { onMonthChanged(it) })
  }
  Row(verticalAlignment = CenterVertically) {
    val prevEnabled = month > YearMonth.of(installDate.year, installDate.month)
    IconButton(
      onClick = { onMonthChanged(month.minusMonths(1)) },
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
      onClick = { showMonthPickerDialog = true },
      colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
      Row(verticalAlignment = CenterVertically) {
        Icon(
          imageVector = Icons.Filled.CalendarToday,
          contentDescription = "Select Date"
        )
        Spacer(modifier = Modifier.width(8.dp)) // Spacing between icon and text
        Text(
          text = month.format()
        )
      }
    }
    val nextEnabled = month < YearMonth.now()
    IconButton(
      { onMonthChanged(month.plusMonths(1)) },
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
        onClick = { onMonthChanged(YearMonth.now()) },
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 32.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colorResource(R.color.solar),
          contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.heightIn(min = 32.dp)
      ) {
        Text(
          text = "This Month",
          fontSize = 10.sp,
        )
      }
    }
  }
}

@Preview(name = "This Month")
@Composable
private fun MonthPickerPreview_Today() {
  MonthPicker(YearMonth.now(), {})
}

@Preview(name = "Last Month")
@Composable
private fun MonthPickerPreview_Yesterday() {
  MonthPicker(YearMonth.now().minusMonths(1), {})
}

@Preview(name = "First Month")
@Composable
private fun MonthPickerPreview_First() {
  MonthPicker(YearMonth.of(INSTALL_DATE.year, INSTALL_DATE.month), {})
}


