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
