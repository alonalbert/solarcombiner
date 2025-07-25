package com.alonalbert.enphase.monitor.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import androidx.core.content.getSystemService

fun Context.checkNetwork() : Boolean {
  val connectivityManager = getSystemService<ConnectivityManager>() ?: return false
  val activeNetwork = connectivityManager.activeNetwork ?: return false
  val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
  return capabilities.hasCapability(NET_CAPABILITY_VALIDATED)
}

