package com.github.whitescent.mastify.database.util

import androidx.room.TypeConverter
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.network.model.status.Status.Mention
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

  @OptIn(ExperimentalSerializationApi::class)
  private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  @TypeConverter
  fun jsonToStatus(json: String): Status = this.json.decodeFromString(json)

  @TypeConverter
  fun statusToJson(status: Status): String = json.encodeToString(status)

  @TypeConverter
  fun jsonToAccount(json: String): Account = this.json.decodeFromString(json)

  @TypeConverter
  fun accountToJson(account: Account): String = json.encodeToString(account)

  @TypeConverter
  fun jsonToTag(json: String): List<Status.Tag> = this.json.decodeFromString(json)

  @TypeConverter
  fun tagToJson(tag: List<Status.Tag>): String = json.encodeToString(tag)

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
}
