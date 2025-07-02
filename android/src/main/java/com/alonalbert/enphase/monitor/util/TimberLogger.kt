package com.alonalbert.enphase.monitor.util

import android.util.Log
import com.alonalbert.solar.combiner.enphase.util.DefaultLogger
import org.slf4j.Marker
import org.slf4j.event.Level
import timber.log.Timber

class TimberLogger : DefaultLogger() {
  override fun handleNormalizedLoggingCall(
    level: Level,
    marker: Marker?,
    messagePattern: String?,
    arguments: Array<out Any?>?,
    throwable: Throwable?
  ) {
    Timber.log(level.priority, throwable, messagePattern, arguments)
  }
}

private val Level.priority
  get() = when (this) {
    Level.ERROR -> Log.ERROR
    Level.WARN -> Log.WARN
    Level.INFO -> Log.INFO
    Level.DEBUG -> Log.DEBUG
    Level.TRACE -> Log.DEBUG
  }