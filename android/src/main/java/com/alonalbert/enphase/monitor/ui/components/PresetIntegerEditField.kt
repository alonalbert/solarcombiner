package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  presets: List<String>,
  valueValidator: (String) -> Boolean = { true },
  keyboardType: KeyboardType = KeyboardType.Text,
  maxDropdownHeight: Dp = 400.dp,
  modifier: Modifier = Modifier,
) {
  var textFieldValue by remember(value) { mutableStateOf(value) }
  var expanded by remember { mutableStateOf(false) }
  var textFieldSize by remember { mutableStateOf(Size.Zero) }

  // Update internal text field if the external value changes
  LaunchedEffect(value) {
    if (value != textFieldValue) {
      textFieldValue = value
    }
  }

  Box(modifier = modifier) {
    OutlinedTextField(
      value = textFieldValue,
      onValueChange = {
        textFieldValue = it
        if (valueValidator(it)) {
          onValueChange(it)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned { coordinates ->
          textFieldSize = coordinates.size.toSize()
        },
      label = { Text(label) },
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      singleLine = true,
      textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
      isError = !valueValidator(textFieldValue),
      trailingIcon = {
        Icon(
          imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
          contentDescription = if (expanded) "Hide Presets" else "Show Presets",
          Modifier.clickable { expanded = !expanded }
        )
      }
    )

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        .heightIn(max = maxDropdownHeight)
    ) {
      presets.forEach { presetValue ->
        DropdownMenuItem(
          text = { Text(presetValue) },
          onClick = {
            textFieldValue = presetValue
            onValueChange(presetValue)
            expanded = false
          }
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PresetIntegerEditFieldPreview() {
  var number by remember { mutableIntStateOf(20) }
  val presets = (5..100 step 5).map { it.toString() }

  MaterialTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row {
          PresetEditField(
            label = "Min Reserve (%)",
            value = number.toString(),
            onValueChange = { number = it.toInt() },
            presets = presets,
            valueValidator = {it.toIntOrNull() != null},
            maxDropdownHeight = 400.dp
          )
        }

        Text("Selected Reserve: $number")
      }
    }
  }
}