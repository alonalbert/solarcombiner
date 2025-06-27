package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.ui.graphics.Path

fun buildPath(block: Path.() -> Unit) : Path = Path().apply(block)