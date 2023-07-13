package com.github.whitescent.mastify.database.util

import androidx.room.TypeConverter
import com.github.whitescent.mastify.network.model.status.Status
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StatusConverter {

  @OptIn(ExperimentalSerializationApi::class)
  private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  @TypeConverter
  fun fromJsonToStatus(json: String): Status {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromStatusToJson(status: Status): String {
    return json.encodeToString(status)
  }

  @TypeConverter
  fun fromJsonToAccount(json: String): Status.Account {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromAccountToJson(account: Status.Account): String {
    return json.encodeToString(account)
  }

  @TypeConverter
  fun fromJsonToTag(json: String): List<Status.Tag> {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromTagToJson(tag: List<Status.Tag>): String {
    return json.encodeToString(tag)
  }

  @TypeConverter
  fun fromJsonToMention(json: String): List<Status.Mention> {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromMentionToJson(mention: List<Status.Mention>): String {
    return json.encodeToString(mention)
  }

  @TypeConverter
  fun fromJsonToApplication(json: String): Status.Application? {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromApplicationToJson(application: Status.Application?): String {
    return json.encodeToString(application)
  }

  @TypeConverter
  fun fromJsonToMediaAttachments(json: String): List<Status.Attachment> {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromMediaAttachmentsToJson(attachments: List<Status.Attachment>): String {
    return json.encodeToString(attachments)
  }
}
