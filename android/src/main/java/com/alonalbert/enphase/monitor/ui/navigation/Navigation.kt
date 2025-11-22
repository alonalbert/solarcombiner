package com.alonalbert.enphase.monitor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.ui.energy.EnergyScreen
import com.alonalbert.enphase.monitor.ui.livestatus.LiveStatusScreen
import com.alonalbert.enphase.monitor.ui.login.LoginScreen
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.Loading
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedIn
import com.alonalbert.enphase.monitor.ui.navigation.NavigationViewModel.LoginState.LoggedOut
import com.alonalbert.enphase.monitor.ui.reserve.ReserveScreen

@Composable
fun MainNavigation() {
  val viewModel: NavigationViewModel = hiltViewModel()

  val navController = rememberNavController()

  val onLoggedIn = {
    navController.navigateToEnergyScreen()
  }

  val onSettings = {
    navController.navigateToLogin()
  }

  val onLiveStatus = {
    navController.navigateToLiveStatus()
  }

  val onReserve = {
    navController.navigateToReserve()
  }

  val loginState by viewModel.loginState.collectAsStateWithLifecycle()

  val startDestination = when (loginState) {
    Loading -> "loading"
    LoggedIn -> "energy"
    LoggedOut -> "login"
  }

  NavHost(navController = navController, startDestination = startDestination) {
    composable("login") {
      LoginScreen(onLoggedIn = onLoggedIn)
    }
    composable("loading") {
      LoadingScreen()
    }
    composable("energy") {
      EnergyScreen(
        onSettings = onSettings,
        onLiveStatus = onLiveStatus,
        onReserve = onReserve,
      )
    }
    composable("live-status") {
      LiveStatusScreen()
    }
    composable("reserve") {
      ReserveScreen({
        viewModel.updateBatteryReserve(it)
        navController.navigateUp()
      })
    }
  }
}

@Preview
@Composable
private fun LoadingScreen() {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .fillMaxSize()
  ) {
    Text(
      text = stringResource(R.string.loading),
      style = MaterialTheme.typography.headlineLarge
    )
  }
}

private fun NavHostController.navigateToLogin() {
  navigate("login")
}

private fun NavHostController.navigateToEnergyScreen() {
  navigate("energy")
}

private fun NavHostController.navigateToLiveStatus() {
  navigate("live-status")
}

private fun NavHostController.navigateToReserve() {
  navigate("reserve")
}
