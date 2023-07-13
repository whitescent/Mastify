package com.github.whitescent.mastify.utils

import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

object FormatFactory {
  fun getInstanceName(url: String): String {
    val regex = Regex("^(?:https?://)?(?:www\\.)?([\\w.-]+)")
    val matchResult = regex.find(url)
    return matchResult?.groups?.get(1)?.value ?: ""
  }
  fun getLocalizedDateTime(timestamp: String): String {
    val zonedDateTime = ZonedDateTime.ofInstant(
      Instant.ofEpochSecond(timestamp.toInstant().epochSeconds),
      ZoneId.systemDefault()
    )
    return zonedDateTime.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
  }
  fun getTime(timestamp: String): String {
    val date = timestamp.toInstant().toEpochMilliseconds()
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(date))
  }
}
