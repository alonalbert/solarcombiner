package com.alonalbert.enphase.monitor.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.Settings
import com.alonalbert.enphase.monitor.ui.components.ButtonComponent
import com.alonalbert.enphase.monitor.ui.components.HeadingTextComponent
import com.alonalbert.enphase.monitor.ui.components.PasswordTextFieldComponent
import com.alonalbert.enphase.monitor.ui.components.TextFieldComponent

@Composable
fun LoginScreen(
  onLoggedIn: () -> Unit,
) {
  val viewModel: LoginViewModel = hiltViewModel()

  val settings by viewModel.settings.collectAsStateWithLifecycle(null)
  LoginScreenContent(settings) {
    if (it.isValid()) {
      viewModel.login(it, onLoggedIn)
    }
  }
}

@Composable
fun LoginScreenContent(
  settings: Settings?,
  onApplyClick: (Settings) -> Unit,
) {
//  var loginInfo by remember { mutableStateOf(enphaseConfig) }

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var mainSiteId by remember { mutableStateOf("") }
  var mainSerialNum by remember { mutableStateOf("") }
  var mainHost by remember { mutableStateOf("") }
  var mainPort by remember { mutableStateOf("") }
  var exportSiteId by remember { mutableStateOf("") }
  var exportSerialNum by remember { mutableStateOf("") }
  var exportHost by remember { mutableStateOf("") }
  var exportPort by remember { mutableStateOf("") }

  if (settings != null) {
    email = settings.email
    password = settings.password
    mainSiteId = settings.mainSiteId
    mainSerialNum = settings.mainSerialNumber
    mainHost = settings.mainHost
    mainPort = settings.mainPort.toString()
    exportSiteId = settings.exportSiteId
    exportSerialNum = settings.exportSerialNumber
    exportHost = settings.exportHost
    exportPort = settings.exportPort.toString()
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(28.dp)
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
      ) {

        HeadingTextComponent(value = stringResource(id = R.string.welcome))
        Spacer(modifier = Modifier.height(20.dp))

        TextFieldComponent(
          text = email,
          labelValue = stringResource(id = R.string.email),
          painterResource(id = R.drawable.email),
          onTextChanged = { email = it },
          isError = email.isBlank()
        )

        PasswordTextFieldComponent(
          text = password,
          labelValue = stringResource(id = R.string.password),
          painterResource(id = R.drawable.lock),
          onTextChanged = { password = it },
          isError = password.isBlank()
        )
        TextFieldComponent(
          text = mainSiteId,
          labelValue = stringResource(id = R.string.main_site_id),
          onTextChanged = { mainSiteId = it },
          isError = mainSiteId.isBlank()
        )
        TextFieldComponent(
          text = mainSerialNum,
          labelValue = stringResource(id = R.string.main_serial_num),
          onTextChanged = { mainSerialNum = it },
          isError = mainSerialNum.isBlank()
        )
        TextFieldComponent(
          text = mainHost,
          labelValue = stringResource(id = R.string.main_host),
          onTextChanged = { mainHost = it },
          isError = mainHost.isBlank()
        )
        TextFieldComponent(
          text = mainPort,
          labelValue = stringResource(id = R.string.main_port),
          onTextChanged = { mainPort = it },
          isError = mainPort.toPort() <= 0,
          keyboardType = KeyboardType.Number,
        )
        TextFieldComponent(
          text = exportSiteId,
          labelValue = stringResource(id = R.string.export_site_id),
          onTextChanged = { exportSiteId = it },
          isError = exportSiteId.isBlank()
        )
        TextFieldComponent(
          text = exportSerialNum,
          labelValue = stringResource(id = R.string.export_serial_num),
          onTextChanged = { exportSerialNum = it },
          isError = exportSerialNum.isBlank()
        )
        TextFieldComponent(
          text = exportHost,
          labelValue = stringResource(id = R.string.export_host),
          onTextChanged = { exportHost = it },
          isError = exportHost.isBlank()
        )
        TextFieldComponent(
          text = exportPort,
          labelValue = stringResource(id = R.string.export_port),
          onTextChanged = { exportPort = it },
          isError = exportPort.toPort() <= 0,
          keyboardType = KeyboardType.Number,
        )

        Spacer(modifier = Modifier.height(40.dp))
        val settings =
          Settings(
            email,
            password,
            mainSiteId,
            mainSerialNum,
            mainHost,
            mainPort.toPort(),
            exportSiteId,
            exportSerialNum,
            exportHost,
            exportPort.toPort()
          )
        ButtonComponent(
          value = stringResource(id = R.string.login),
          onButtonClicked = {
            onApplyClick(settings)
          },
          isEnabled = settings.isValid(),
        )
      }
    }
  }
}

private fun String.toPort() = toIntOrNull() ?: 0

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginScreen() {
  LoginScreenContent(settings = null, onApplyClick = {})
}