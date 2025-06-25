package com.alonalbert.enphase.monitor.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val StopTimeoutMillis: Long = 5000

/**
 * A [SharingStarted] meant to be used with a [kotlinx.coroutines.flow.StateFlow] to expose data to the UI.
 *
 * When the UI stops observing, upstream flows stay active for some time to allow the system to
 * come back from a short-lived configuration change (such as rotations). If the UI stops
 * observing for longer, the cache is kept but the upstream flows are stopped. When the UI comes
 * back, the latest value is replayed and the upstream flows are executed again. This is done to
 * save resources when the app is in the background but let users switch between apps quickly.
 */
private val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(StopTimeoutMillis)

fun <T> Flow<T>.stateIn(
  scope: CoroutineScope,
  initialValue: T
): StateFlow<T> {
  return stateIn(scope, WhileUiSubscribed, initialValue)
}
