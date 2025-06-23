package com.alonalbert.enphase.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.alonalbert.enphase.monitor.ui.energy.EnergyScreen
import com.alonalbert.enphase.monitor.ui.theme.SolarCombinerTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SolarCombinerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          EnergyScreen(
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
