package com.alonalbert.enphase.monitor

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alonalbert.enphase.monitor.PermissionStatus.Acknowledged
import com.alonalbert.enphase.monitor.PermissionStatus.Denied
import com.alonalbert.enphase.monitor.PermissionStatus.Granted
import com.alonalbert.enphase.monitor.PermissionStatus.NotGranted
import com.alonalbert.enphase.monitor.services.AlarmReceiver
import com.alonalbert.enphase.monitor.ui.navigation.MainNavigation
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SolarCombinerTheme {
        Surface {
          HandleExactAlarmPermission {
            MainNavigation()
          }
        }
      }
    }
  }
}

@Composable
private fun HandleExactAlarmPermission(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val alarmManager = context.getSystemService<AlarmManager>()!!

  var permissionState by remember { mutableStateOf(PermissionStatus.Unknown) }

  LaunchedEffect(key1 = context) {
    if (alarmManager.canScheduleExactAlarms()) {
      Timber.i("Exact Alarm permission already granted on initial check.")
      permissionState = Granted
    } else {
      Timber.i("Exact Alarm permission not granted on initial check. Requesting.")
      permissionState = NotGranted
    }
  }


  when (permissionState) {
    PermissionStatus.Unknown -> {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Checking permissions...")
      }
    }

    Granted -> {
      LaunchedEffect(Unit) {
        Timber.d("Permission Granted state: Scheduling alarm and proceeding.")
        AlarmReceiver.scheduleAlarm(context)
      }
      content()
    }

    NotGranted -> AskForPermission { permissionState = it }
    Denied -> PermissionDenied { permissionState = Acknowledged }
    Acknowledged -> content()
  }
}

@Composable
private fun AskForPermission(onResult: (PermissionStatus) -> Unit) {
  val context = LocalContext.current
  val alarmManager = context.getSystemService<AlarmManager>()!!

  val scheduleExactAlarmPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { _ ->
    val status = if (alarmManager.canScheduleExactAlarms()) Granted else Denied
    Timber.i("Exact Alarm permission $status after returning from settings.")
    onResult(status)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      "This app needs permission to schedule exact alarms for its core functionality. " +
          "Please grant this permission in the next screen.",
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(bottom = 16.dp)
    )
    Button(onClick = {
      Intent().apply {
        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
        data = Uri.fromParts("package", context.packageName, null)
        scheduleExactAlarmPermissionLauncher.launch(this)
      }
    }) {
      Text("Open Settings to Grant Permission")
    }
  }

}

@Composable
private fun PermissionDenied(onAcknowledged: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      "Exact alarm permission is required for the app to function correctly. " +
          "The permission was not granted.",
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.padding(bottom = 16.dp)
    )
    Button(onClick = onAcknowledged) {
      Text("Acknowledge")
    }
  }
}


private enum class PermissionStatus {
  Unknown,
  Granted,
  NotGranted,
  Denied,
  Acknowledged,
}
