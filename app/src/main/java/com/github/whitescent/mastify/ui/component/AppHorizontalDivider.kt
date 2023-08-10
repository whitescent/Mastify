package com.github.whitescent.mastify.ui.component

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun AppHorizontalDivider() = HorizontalDivider(thickness = 0.5.dp, color = AppTheme.colors.divider)
