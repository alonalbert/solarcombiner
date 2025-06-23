package com.alonalbert.enphase.monitor.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM

fun LocalDate.format(): String = format(DateTimeFormatter.ofLocalizedDate(MEDIUM))
