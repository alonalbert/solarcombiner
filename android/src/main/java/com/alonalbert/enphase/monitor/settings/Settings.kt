package com.alonalbert.enphase.monitor.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val EMAIL = stringPreferencesKey("email")
val PASSWORD = stringPreferencesKey("password")
val MAIN_SITE_ID = stringPreferencesKey("mainSiteId")
val MAIN_SERIAL_NUM = stringPreferencesKey("mainSerialNum")
val MAIN_HOST = stringPreferencesKey("mainHost")
val MAIN_PORT = intPreferencesKey("mainPort")
val EXPORT_SITE_ID = stringPreferencesKey("exportSiteId")
val EXPORT_SERIAL_NUM = stringPreferencesKey("exportSerialNum")
val EXPORT_HOST = stringPreferencesKey("exportHost")
val EXPORT_PORT = intPreferencesKey("exportPort")
val LOGGED_IN = booleanPreferencesKey("loggedIn")

val Preferences.email get() = get(EMAIL) ?: ""
val Preferences.password get() = get(PASSWORD) ?: ""
val Preferences.mainSiteId get() = get(MAIN_SITE_ID) ?: ""
val Preferences.mainSerialNum get() = get(MAIN_SERIAL_NUM) ?: ""
val Preferences.mainHost get() = get(MAIN_HOST) ?: ""
val Preferences.mainPort get() = get(MAIN_PORT) ?: 80
val Preferences.exportSiteId get() = get(EXPORT_SITE_ID) ?: ""
val Preferences.exportSerialNum get() = get(EXPORT_SERIAL_NUM) ?: ""
val Preferences.exportHost get() = get(EXPORT_HOST) ?: ""
val Preferences.exportPort get() = get(EXPORT_PORT) ?: 80
val Preferences.loggedIn get() = get(LOGGED_IN) ?: ""

fun Application.updateSettings(scope: CoroutineScope, block: suspend MutablePreferences.() -> Unit) {
  scope.launch {
    dataStore.updateData {
      it.toMutablePreferences().apply {
        block()
      }
    }
  }
}
