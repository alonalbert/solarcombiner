package com.alonalbert.enphase.monitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PullToRefresh(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  state: PullToRefreshState = rememberPullToRefreshState(),
  contentAlignment: Alignment = Alignment.TopStart,
  indicator: @Composable BoxScope.() -> Unit = {
    Indicator(
      modifier = Modifier.align(Alignment.TopCenter),
      isRefreshing = isRefreshing,
      state = state,
    )
  },
  content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
  PullToRefreshBox(isRefreshing, onRefresh, modifier, state, contentAlignment, indicator) {
    BoxWithConstraints {
      val scrollState = rememberScrollState()
      Box(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)) {
        this@BoxWithConstraints.content()
      }
    }
  }
}
