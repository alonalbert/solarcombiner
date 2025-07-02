package com.alonalbert.solar.combiner.enphase.util

import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.LegacyAbstractLogger

open class DefaultLogger : LegacyAbstractLogger() {
  override fun isTraceEnabled() = true

  override fun isDebugEnabled() = true

  override fun isInfoEnabled() = true

  override fun isWarnEnabled() = true

  override fun isErrorEnabled() = true

  override fun getFullyQualifiedCallerName() = ""

  override fun handleNormalizedLoggingCall(
    level: Level,
    marker: Marker?,
    messagePattern: String?,
    arguments: Array<out Any?>?,
    throwable: Throwable?
  ) {
    val stream = if (level == Level.ERROR) System.err else System.out
    if (messagePattern != null) {
      when (arguments) {
        null -> stream.println(messagePattern)
        else -> stream.println(messagePattern.format(*arguments))
      }
    }
    throwable?.printStackTrace(stream)
  }
}