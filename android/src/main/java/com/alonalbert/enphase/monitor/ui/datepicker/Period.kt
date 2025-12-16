package com.alonalbert.enphase.monitor.ui.datepicker

import com.alonalbert.enphase.monitor.util.nowAtSite

sealed class Period {

  companion object {
    fun today() = DayPeriod(nowAtSite().atStartOfDay().toLocalDate())
  }
}