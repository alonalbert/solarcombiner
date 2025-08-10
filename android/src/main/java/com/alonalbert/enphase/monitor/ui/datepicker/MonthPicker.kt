package com.alonalbert.enphase.monitor.ui.datepicker

import androidx.compose.runtime.Composable
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthPicker(
  month: YearMonth,
  onMonthChanged: (YearMonth) -> Unit,
  installDate: LocalDate = INSTALL_DATE,
) {
  TODO("$month $onMonthChanged $installDate")
}
