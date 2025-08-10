package com.alonalbert.enphase.monitor.ui.energy

import java.time.LocalDate
import java.time.YearMonth

sealed class Period {
  class DayPeriod(val day: LocalDate): Period()
  class MonthPeriod(val month: YearMonth): Period()

  companion object {
    fun today() = DayPeriod(LocalDate.now().atStartOfDay().toLocalDate())
  }
}
