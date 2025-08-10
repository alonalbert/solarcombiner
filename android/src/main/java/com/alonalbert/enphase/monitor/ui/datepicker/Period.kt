package com.alonalbert.enphase.monitor.ui.datepicker

import java.time.LocalDate

sealed class Period {

  companion object {
    fun today() = DayPeriod(LocalDate.now().atStartOfDay().toLocalDate())
  }
}