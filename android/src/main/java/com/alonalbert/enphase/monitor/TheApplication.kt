package com.alonalbert.enphase.monitor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class TheApplication : Application() {
  override fun onCreate() {
    super.onCreate()

    Timber.plant(object : Timber.DebugTree() {
      override fun createStackElementTag(element: StackTraceElement) =
        "SolarCombiner (${element.fileName}:${element.lineNumber})"
    })
  }
}
