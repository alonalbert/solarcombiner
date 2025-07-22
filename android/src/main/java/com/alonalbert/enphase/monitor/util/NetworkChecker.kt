package com.alonalbert.enphase.monitor.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import androidx.core.content.getSystemService

object NetworkChecker {
  fun checkNetwork(context: Context) : Boolean {
    val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NET_CAPABILITY_VALIDATED)
  }

}