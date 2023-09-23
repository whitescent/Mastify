/*
 * Copyright 2023 WhiteScent
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

package com.github.whitescent.mastify.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.emoji.Emoji

@Entity
data class InstanceEntity(
  @PrimaryKey val instance: String,
  val emojiList: List<Emoji>?,
  val maximumTootCharacters: Int?,
  val maxPollOptions: Int?,
  val maxPollCharactersPerOption: Int?,
  val minPollExpiration: Int?,
  val maxPollExpiration: Int?,
  val videoSizeLimit: Int?,
  val imageSizeLimit: Int?,
  val imageMatrixLimit: Int?,
  val maxMediaAttachments: Int?
)

data class EmojisEntity(
  @PrimaryKey val instance: String,
  val emojiList: List<Emoji>?,
)

data class InstanceInfoEntity(
  @PrimaryKey val instance: String,
  val maximumTootCharacters: Int?,
  val maxPollOptions: Int?,
  val maxPollCharactersPerOption: Int?,
  val minPollExpiration: Int?,
  val maxPollExpiration: Int?,
  val videoSizeLimit: Int?,
  val imageSizeLimit: Int?,
  val imageMatrixLimit: Int?,
  val maxMediaAttachments: Int?
)
