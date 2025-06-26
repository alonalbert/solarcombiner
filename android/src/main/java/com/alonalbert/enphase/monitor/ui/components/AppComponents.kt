package com.alonalbert.enphase.monitor.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alonalbert.enphase.monitor.R

@Suppress("unused")
@Composable
fun NormalTextComponent(value: String) {
  Text(
    text = value,
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 40.dp),
    style = TextStyle(
      fontSize = 24.sp,
      fontWeight = FontWeight.Normal,
      fontStyle = FontStyle.Normal
    ),
    color = colorScheme.onBackground,
    textAlign = TextAlign.Center
  )
}

@Composable
fun HeadingTextComponent(value: String) {
  Text(
    text = value,
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(),
    style = TextStyle(
      fontSize = 30.sp,
      fontWeight = FontWeight.Bold,
      fontStyle = FontStyle.Normal
    ),
    color = colorScheme.onBackground,
    textAlign = TextAlign.Center
  )
}

@Composable
fun TextFieldComponent(
  text: String,
  labelValue: String,
  painterResource: Painter? = null,
  onTextChanged: (String) -> Unit,
  isError: Boolean = false,
  keyboardType: KeyboardType = KeyboardType.Unspecified,
) {

  OutlinedTextField(
    modifier = Modifier
      .fillMaxWidth(),
    label = { Text(text = labelValue) },
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = colorScheme.primary,
      focusedLabelColor = colorScheme.primary,
      cursorColor = colorScheme.primary,
    ),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = keyboardType),
    singleLine = true,
    maxLines = 1,
    value = text,
    onValueChange = onTextChanged,
    leadingIcon = when (painterResource) {
      null -> null
      else -> {
        { Icon(painter = painterResource, contentDescription = "") }
      }
    },
    isError = isError
  )
}


@Composable
fun PasswordTextFieldComponent(
  text: String,
  labelValue: String,
  painterResource: Painter? = null,
  onTextChanged: (String) -> Unit,
  isError: Boolean = false
) {

  val localFocusManager = LocalFocusManager.current

  val passwordVisible = remember { mutableStateOf(false) }

  OutlinedTextField(
    modifier = Modifier
      .fillMaxWidth(),
    label = { Text(text = labelValue) },
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = colorScheme.primary,
      focusedLabelColor = colorScheme.primary,
      cursorColor = colorScheme.primary,
    ),
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Password,
      imeAction = ImeAction.Done
    ),
    singleLine = true,
    keyboardActions = KeyboardActions {
      localFocusManager.clearFocus()
    },
    maxLines = 1,
    value = text,
    onValueChange = onTextChanged,
    leadingIcon = when (painterResource) {
      null -> null
      else -> {
        { Icon(painter = painterResource, contentDescription = "") }
      }
    },
    trailingIcon = {

      val iconImage = if (passwordVisible.value) {
        Icons.Filled.Visibility
      } else {
        Icons.Filled.VisibilityOff
      }

      val description = if (passwordVisible.value) {
        stringResource(id = R.string.hide_password)
      } else {
        stringResource(id = R.string.show_password)
      }

      IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
        Icon(imageVector = iconImage, contentDescription = description)
      }

    },
    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
    isError = isError
  )
}

@Composable
fun ButtonComponent(value: String, onButtonClicked: () -> Unit, isEnabled: Boolean = false) {
  Button(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(48.dp),
    onClick = {
      onButtonClicked.invoke()
    },
    contentPadding = PaddingValues(),
    colors = ButtonDefaults.buttonColors(Color.Transparent),
    shape = RoundedCornerShape(50.dp),
    enabled = isEnabled
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(48.dp)
        .background(
          color = if (isEnabled) colorScheme.primary else colorScheme.primary.copy(alpha = 0.4f),
          shape = RoundedCornerShape(50.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = value,
        fontSize = 18.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold
      )

    }

  }
}


@Preview(
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun TextFieldComponentPreview() {
  TextFieldComponent(
    text = "",
    labelValue = "Label",
    painterResource = null,
    onTextChanged = {}
  )
}


