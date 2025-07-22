package com.alonalbert.enphase.monitor

import android.app.Application
import android.util.Log.DEBUG
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.BackoffPolicy.LINEAR
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest.Companion.MIN_PERIODIC_INTERVAL_MILLIS
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import com.alonalbert.enphase.monitor.ui.reserve.ReserveManagerWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject


@HiltAndroidApp
class TheApplication : Application(), Configuration.Provider {
  // Field injection is the only option for an Application
  @Inject
  lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory via field injection

  override fun onCreate() {
    super.onCreate()

    Timber.plant(object : Timber.DebugTree() {
      override fun createStackElementTag(element: StackTraceElement) =
        "EnphaseMonitor (${element.fileName}:${element.lineNumber})"
    })
    schedulePeriodicWork()
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .setMinimumLoggingLevel(DEBUG)
      .build()

  private fun schedulePeriodicWork() {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(CONNECTED)
      .build()

    val interval = MIN_PERIODIC_INTERVAL_MILLIS
    val request =
      PeriodicWorkRequestBuilder<ReserveManagerWorker>(interval, MILLISECONDS)
        .setConstraints(constraints)
        .addTag(ReserveManagerWorker.TAG)
        .setBackoffCriteria(LINEAR, MIN_BACKOFF_MILLIS, MILLISECONDS)
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
      ReserveManagerWorker.TAG,
      ExistingPeriodicWorkPolicy.REPLACE,
      request
    )
  }
}
