package com.alonalbert.enphase.monitor.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.LoginInfo
import com.alonalbert.enphase.monitor.ui.components.ButtonComponent
import com.alonalbert.enphase.monitor.ui.components.HeadingTextComponent
import com.alonalbert.enphase.monitor.ui.components.PasswordTextFieldComponent
import com.alonalbert.enphase.monitor.ui.components.TextFieldComponent
import com.alonalbert.enphase.monitor.util.stringResourceOrDefault

@Composable
fun LoginScreen(
  onLoggedIn: () -> Unit,
) {
  val viewModel: LoginViewModel = hiltViewModel()

  val loginInfo by viewModel.loginInfo.collectAsStateWithLifecycle(null)
  LoginScreenContent(loginInfo) {
    viewModel.login(it, onLoggedIn)
  }
}

@Composable
fun LoginScreenContent(
  loginInfo: LoginInfo?,
  onApplyClick: (LoginInfo) -> Unit,
) {

  val serverValue = stringResourceOrDefault(R.string.server, "")
  val usernameValue = stringResourceOrDefault(R.string.username, "")
  val passwordValue = stringResourceOrDefault(R.string.password, "")

  var server by remember { mutableStateOf(serverValue) }
  var username by remember { mutableStateOf(usernameValue) }
  var password by remember { mutableStateOf(passwordValue) }

  if (loginInfo != null) {
    server = loginInfo.server
    username = loginInfo.username
    password = loginInfo.password
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
      val scrollState = rememberScrollState()
      Column(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
          .verticalScroll(scrollState)
      ) {

        HeadingTextComponent(value = stringResource(id = R.string.welcome))
        Spacer(modifier = Modifier.height(20.dp))

        TextFieldComponent(
          text = server,
          labelValue = stringResource(id = R.string.server),
          painterResource(id = R.drawable.server),
          onTextChanged = { server = it },
          isError = server.isBlank()
        )

        TextFieldComponent(
          text = username,
          labelValue = stringResource(id = R.string.username),
          painterResource(id = R.drawable.email),
          onTextChanged = { username = it },
          isError = username.isBlank()
        )

        PasswordTextFieldComponent(
          text = password,
          labelValue = stringResource(id = R.string.password_label),
          painterResource(id = R.drawable.lock),
          onTextChanged = { password = it },
          isError = password.isBlank()
        )

        Spacer(modifier = Modifier.height(40.dp))
        val loginInfo =
          LoginInfo(
            server,
            username,
            password,
          )
        ButtonComponent(
          value = stringResource(id = R.string.login),
          onButtonClicked = {
            onApplyClick(loginInfo)
          },
          isEnabled = loginInfo.isValid()
        )
      }
    }
  }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginScreen() {
  LoginScreenContent(loginInfo = null, onApplyClick = {})
}