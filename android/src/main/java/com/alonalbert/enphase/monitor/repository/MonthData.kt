package com.alonalbert.enphase.monitor.repository

import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.ui.datepicker.MonthPeriod
import java.time.YearMonth

class MonthData(val month: YearMonth, val days: List<DayTotals>): ChartData(MonthPeriod(month))