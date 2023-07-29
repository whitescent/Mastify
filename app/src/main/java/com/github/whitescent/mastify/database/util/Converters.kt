package com.github.whitescent.mastify.database.util

import androidx.room.TypeConverter
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.network.model.status.Status.Mention
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
  fun fromJsonToAccount(json: String): Account {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromAccountToJson(account: Account): String {
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
  fun fromJsonToFields(json: String): List<Fields> {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromFieldsToJson(mention: List<Fields>): String {
    return json.encodeToString(mention)
  }

  @TypeConverter
  fun fromJsonToMention(json: String): List<Mention> {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromMentionToJson(mention: List<Mention>): String {
    return json.encodeToString(mention)
  }

  @TypeConverter
  fun fromJsonToApplication(json: String): Application? {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromApplicationToJson(application: Application?): String {
    return json.encodeToString(application)
  }

  @TypeConverter
  fun fromJsonToMediaAttachments(json: String): List<Attachment> {
    return this.json.decodeFromString(json)
  }

  @TypeConverter
  fun fromMediaAttachmentsToJson(attachments: List<Attachment>): String {
    return json.encodeToString(attachments)
  }
}
