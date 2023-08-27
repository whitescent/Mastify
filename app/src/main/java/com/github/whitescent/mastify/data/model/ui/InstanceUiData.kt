package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Stable
import com.github.whitescent.mastify.data.repository.InstanceRepository

@Stable
data class InstanceUiData(
  val instanceTitle: String = "",
  val activeMonth: Int = 0,
  val instanceImageUrl: String = "",
  val instanceDescription: String = "",
  val maximumTootCharacters: Int? = InstanceRepository.DEFAULT_CHARACTER_LIMIT,
  val maxPollOptions: Int? = null,
  val maxPollCharactersPerOption: Int? = null,
  val minPollExpiration: Int? = null,
  val maxPollExpiration: Int? = null,
  val videoSizeLimit: Int? = null,
  val imageSizeLimit: Int? = null,
  val imageMatrixLimit: Int? = null,
  val maxMediaAttachments: Int = InstanceRepository.DEFAULT_MAX_MEDIA_ATTACHMENTS
)
