package com.alonalbert.solar.combiner.enphase.util

import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.LegacyAbstractLogger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.HOUR_OF_DAY
import java.time.temporal.ChronoField.MINUTE_OF_HOUR
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.SECOND_OF_MINUTE
import java.time.temporal.ChronoField.YEAR

open class DefaultLogger : LegacyAbstractLogger() {
  override fun isTraceEnabled() = true

  override fun isDebugEnabled() = true

  override fun isInfoEnabled() = true

  override fun isWarnEnabled() = true

  override fun isErrorEnabled() = true

  override fun getFullyQualifiedCallerName() = ""

  private val formatter = DateTimeFormatterBuilder()
    .appendValue(YEAR)
    .appendLiteral('-')
    .appendValue(MONTH_OF_YEAR, 2)
    .appendLiteral('-')
    .appendValue(DAY_OF_MONTH, 2)
    .appendLiteral(' ')
    .appendValue(HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(MINUTE_OF_HOUR, 2)
    .appendLiteral(':')
    .appendValue(SECOND_OF_MINUTE)
    .toFormatter()

  override fun handleNormalizedLoggingCall(
    level: Level,
    marker: Marker?,
    messagePattern: String?,
    arguments: Array<out Any?>?,
    throwable: Throwable?
  ) {
    val now = LocalDateTime.now()
    val stream = if (level == Level.ERROR) System.err else System.out
    val message = buildString {
      append(now.format(formatter))
      append(": ")
      if (messagePattern != null) {
        when (arguments) {
          null -> appendLine(messagePattern)
          else -> appendLine(messagePattern.format(*arguments))
        }
      }
    }
    stream.print(message)
    throwable?.printStackTrace(stream)
  }
}
