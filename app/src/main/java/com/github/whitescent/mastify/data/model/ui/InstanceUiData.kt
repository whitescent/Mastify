package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Stable

@Stable
data class InstanceUiData(
  val instanceTitle: String = "",
  val activeMonth: Int = 0,
  val instanceImageUrl: String = "",
  val instanceDescription: String = "",
)
