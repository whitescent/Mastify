package com.github.whitescent.mastify.ui.component.status.paging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun EmptyStatusListPlaceholder() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center
  ) { Text("你似乎还没关注其他人哦", fontSize = 18.sp) }
}
