/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

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
