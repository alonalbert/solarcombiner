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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.ui.components.ButtonComponent
import com.alonalbert.enphase.monitor.ui.components.HeadingTextComponent
import com.alonalbert.enphase.monitor.ui.components.PasswordTextFieldComponent
import com.alonalbert.enphase.monitor.ui.components.TextFieldComponent
import com.alonalbert.enphase.monitor.ui.login.LoginViewModel.LoginInfo

@Composable
fun LoginScreen(
  onLoggedIn: () -> Unit,
) {
  val viewModel: LoginViewModel = hiltViewModel()

  val loginState by viewModel.loginInfo.collectAsStateWithLifecycle(null)

  LoginScreenContent(loginState) { username, password, innerSystemId, outerSystemId ->
    viewModel.login(username, password, innerSystemId, outerSystemId, onLoggedIn)
  }
}

@Composable
fun LoginScreenContent(
  loginInfo: LoginInfo?,
  onConnectClick: (String, String, String, String) -> Unit,
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var innerSystemId by remember { mutableStateOf("") }
  var outerSystemId by remember { mutableStateOf("") }

  if (loginInfo != null) {
    email = loginInfo.email
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
          text = innerSystemId,
          labelValue = stringResource(id = R.string.inner_system_id),
          onTextChanged = { innerSystemId = it },
          isError = innerSystemId.isBlank()
        )
        TextFieldComponent(
          text = outerSystemId,
          labelValue = stringResource(id = R.string.outer_system_id),
          onTextChanged = { outerSystemId = it },
          isError = outerSystemId.isBlank()
        )


        Spacer(modifier = Modifier.height(40.dp))
        ButtonComponent(
          value = stringResource(id = R.string.login),
          onButtonClicked = {
            onConnectClick(email, password, innerSystemId, outerSystemId)
          },
          isEnabled = email.isNotEmpty() && password.isNotEmpty() && innerSystemId.isNotEmpty() && outerSystemId.isNotEmpty()
        )

      }

    }
  }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginScreen() {
  LoginScreenContent(loginInfo = LoginInfo(), onConnectClick = { _, _, _, _ -> })
}