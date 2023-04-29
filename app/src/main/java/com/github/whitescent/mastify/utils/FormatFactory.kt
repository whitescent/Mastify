package com.github.whitescent.mastify.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration


object FormatFactory {
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
}
