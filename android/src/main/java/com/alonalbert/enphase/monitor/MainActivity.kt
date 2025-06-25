package com.alonalbert.enphase.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alonalbert.enphase.monitor.ui.navigation.MainNavigation
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SolarCombinerTheme {
        MainNavigation()
      }
    }
  }
}
