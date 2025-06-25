package com.alonalbert.enphase.monitor

import android.app.Application
import com.alonalbert.enphase.monitor.settings.dataStore
import com.alonalbert.enphase.monitor.settings.email
import com.alonalbert.enphase.monitor.settings.innerSystemId
import com.alonalbert.enphase.monitor.settings.outerSystemId
import com.alonalbert.enphase.monitor.settings.password
import com.alonalbert.solar.combiner.enphase.Enphase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import timber.log.Timber


@HiltAndroidApp
class TheApplication : Application() {
  private lateinit var _enphase: Enphase
  val enphase get() = _enphase

  override fun onCreate() {
    super.onCreate()

    Timber.plant(object : Timber.DebugTree() {
      override fun createStackElementTag(element: StackTraceElement) =
        "SolarCombiner (${element.fileName}:${element.lineNumber})"
    })
  }

  suspend fun login() {
    val preferences = dataStore.data.first()
    _enphase = Enphase.create(preferences.email, preferences.password, preferences.innerSystemId, preferences.outerSystemId, cacheDir.toPath() )
  }
}
