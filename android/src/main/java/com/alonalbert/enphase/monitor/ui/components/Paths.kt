package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

fun buildPath(block: Path.() -> Unit) : Path = Path().apply(block)

fun Path.moveTo(offset: Offset) = moveTo(offset.x, offset.y)

fun Path.lineTo(offset: Offset) = lineTo(offset.x, offset.y)