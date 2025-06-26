package com.alonalbert.enphase.monitor.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val EMAIL = stringPreferencesKey("email")
val PASSWORD = stringPreferencesKey("password")
val INNER_SYSTEM_ID = stringPreferencesKey("innerSystemId")
val OUTER_SYSTEM_ID = stringPreferencesKey("outerSystemId")
val LOGGED_IN = booleanPreferencesKey("loggedIn")

val Preferences.email get() = get(EMAIL) ?:""
val Preferences.password get() = get(PASSWORD) ?:""
val Preferences.mainSiteId get() = get(INNER_SYSTEM_ID) ?:""
val Preferences.exportSiteId get() = get(OUTER_SYSTEM_ID) ?:""
val Preferences.loggedIn get() = get(LOGGED_IN) ?:""

fun Application.updateSettings(scope: CoroutineScope, block: suspend MutablePreferences.() -> Unit) {
  scope.launch {
    dataStore.updateData {
      it.toMutablePreferences().apply {
        block()
      }
    }
  }
}
