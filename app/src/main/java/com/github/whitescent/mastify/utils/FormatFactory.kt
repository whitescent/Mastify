package com.github.whitescent.mastify.utils

import kotlinx.datetime.toInstant
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatFactory {
  fun getInstanceName(url: String): String {
    val regex = Regex("^(?:https?://)?(?:www\\.)?([\\w.-]+)")
    val matchResult = regex.find(url)
    return matchResult?.groups?.get(1)?.value ?: ""
  }
  fun getLocalizedDateTime(timestamp: String): String {
    return DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault())
      .format(timestamp.toInstant().toEpochMilliseconds())
  }
  fun getTime(timestamp: String): String {
    val date = timestamp.toInstant().toEpochMilliseconds()
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(date))
  }
}
