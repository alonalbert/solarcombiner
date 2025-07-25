package com.alonalbert.enphase.monitor.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
      intent.action == "android.intent.action.QUICKBOOT_POWERON"
    ) {
      Timber.d("BootReceiver: Device booted or quick boot power on. Rescheduling alarm.")
      AlarmReceiver.scheduleAlarm(context)
    }
  }
}
