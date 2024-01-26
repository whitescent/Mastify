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

package com.github.whitescent.mastify.database.util

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Card
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.Mention
import com.github.whitescent.mastify.network.model.status.Poll
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@ProvidedTypeConverter
@Singleton
class Converters @Inject constructor(private val json: Json) {

  @TypeConverter
  fun jsonToStatus(json: String): Status = this.json.decodeFromString(json)

  @TypeConverter
  fun statusToJson(status: Status): String = json.encodeToString(status)

  @TypeConverter
  fun jsonToAccount(json: String): Account = this.json.decodeFromString(json)

  @TypeConverter
  fun accountToJson(account: Account): String = json.encodeToString(account)

  @TypeConverter
  fun jsonToTag(json: String): List<Hashtag> = this.json.decodeFromString(json)

  @TypeConverter
  fun tagToJson(tag: List<Hashtag>): String = json.encodeToString(tag)

  @TypeConverter
  fun jsonToFields(json: String): List<Fields> = this.json.decodeFromString(json)

  @TypeConverter
  fun fieldsToJson(mention: List<Fields>): String = json.encodeToString(mention)

  @TypeConverter
  fun jsonToMention(json: String): List<Mention> = this.json.decodeFromString(json)

  @TypeConverter
  fun mentionToJson(mention: List<Mention>): String = json.encodeToString(mention)

  @TypeConverter
  fun jsonToApplication(json: String): Application? = this.json.decodeFromString(json)

  @TypeConverter
  fun applicationToJson(application: Application?): String = json.encodeToString(application)

  @TypeConverter
  fun jsonToMediaAttachments(json: String): List<Attachment> = this.json.decodeFromString(json)

  @TypeConverter
  fun mediaAttachmentsToJson(attachments: List<Attachment>): String = json.encodeToString(attachments)

  @TypeConverter
  fun jsonToEmoji(json: String): List<Emoji> = this.json.decodeFromString(json)

  @TypeConverter
  fun emojiToJson(emoji: List<Emoji>): String = json.encodeToString(emoji)

  @TypeConverter
  fun jsonToPoll(json: String): Poll? = this.json.decodeFromString(json)

  @TypeConverter
  fun pollToJson(poll: Poll?): String = json.encodeToString(poll)

  @TypeConverter
  fun jsonToCard(json: String): Card = this.json.decodeFromString(json)

  @TypeConverter
  fun cardToJson(card: Card): String = json.encodeToString(card)
}
