package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Stable

@Stable
data class InstanceUiData(
  val instanceTitle: String = "",
  val activeMonth: Int = 0,
  val instanceImageUrl: String = "",
  val instanceDescription: String = "",
  val maximumTootCharacters: Int? = null,
  val maxPollOptions: Int? = null,
  val maxPollCharactersPerOption: Int? = null,
  val minPollExpiration: Int? = null,
  val maxPollExpiration: Int? = null,
  val videoSizeLimit: Int? = null,
  val imageSizeLimit: Int? = null,
  val imageMatrixLimit: Int? = null,
  val maxMediaAttachments: Int = 4
)
