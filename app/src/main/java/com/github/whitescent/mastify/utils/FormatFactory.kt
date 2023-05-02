package com.github.whitescent.mastify.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration


object FormatFactory {
  fun getInstanceName(url: String): String {
    val regex = Regex("^(?:https?:\\/\\/)?(?:www\\.)?([\\w.-]+)")
    val matchResult = regex.find(url)
    return matchResult?.groups?.get(1)?.value ?: ""
  }
  fun getTimeDiff(time: String): String {
    val now = Clock.System.now()
    val instantInThePast: Instant = Instant.parse(time)
    val durationSinceThen: Duration = now - instantInThePast
    val dayDiff = durationSinceThen.inWholeDays
    val hoursDiff = durationSinceThen.inWholeHours
    val minutesDiff = durationSinceThen.inWholeMinutes
    val secDiff = durationSinceThen.inWholeSeconds
    // TODO: Need to update strings for localization
    return when {
      dayDiff in 1..1 -> "昨天"
      dayDiff > 1 -> "$dayDiff 天前"
      hoursDiff > 0 -> "$hoursDiff 小时前"
      minutesDiff > 0 -> "$minutesDiff 分钟前"
      else -> "$secDiff 秒前"
    }
  }
  fun getTimeYear(time: String): Int =
    Instant.parse(time).toLocalDateTime(TimeZone.UTC).year
  fun getTimeMouth(time: String): Int =
    Instant.parse(time).toLocalDateTime(TimeZone.UTC).monthNumber
  fun getTimeDayOfMonth(time: String): Int =
    Instant.parse(time).toLocalDateTime(TimeZone.UTC).dayOfMonth
}
