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

package com.github.whitescent.mastify.utils

import android.icu.text.DateFormat
import android.text.format.DateUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.net.MalformedURLException
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

object FormatFactory {
  fun getInstanceName(url: String): String {
    val regex = Regex("^(?:https?://)?(?:www\\.)?([\\w.-]+)")
    val matchResult = regex.find(url)
    return matchResult?.groups?.get(1)?.value ?: ""
  }

  fun getAcctFromUrl(mastodonUrl: String): String {
    val url = try {
      java.net.URL(mastodonUrl)
    } catch (e: MalformedURLException) {
      throw IllegalArgumentException("Invalid URL format")
    }
    val domain = url.host
    val path = url.path
    val username = path.substringAfterLast("@")
    return "$username@$domain"
  }

  fun getLocalizedDateTime(timestamp: String): String {
    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    val targetYear = timestamp.toInstant().toLocalDateTime(TimeZone.currentSystemDefault()).year

    val pattern = if (currentYear == targetYear) {
      DateFormat.ABBR_MONTH_DAY
    } else {
      DateFormat.YEAR_ABBR_MONTH_DAY
    }
    val formatter = DateFormat.getPatternInstance(pattern, Locale.getDefault())
    return formatter.format(Date.from(timestamp.toInstant().toJavaInstant()))
  }

  // This function follows the locale format, not the system format (24 hour or 12 hour mode).
  // If the system format is desired, use [android.text.format.DateFormat] (requires a Context).
  fun getTime(timestamp: String): String {
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
    return formatter.format(Date.from(timestamp.toInstant().toJavaInstant()))
  }

  fun getPercentageString(value: Float): String {
    val percentInstance = NumberFormat.getPercentInstance()
    percentInstance.maximumFractionDigits = if (value > 0.01f) 0 else 2
    return percentInstance.format(value)
  }

  fun ensureHttpPrefix(url: String): String {
    return if (!url.startsWith("http://") && !url.startsWith("https://")) {
      return "https://$url"
    } else url
  }

  fun isValidUrl(url: String): Boolean {
    val urlRegex = (
      "^(https?://)?" +
        "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" +
        "localhost|" +
        "((\\d{1,3}\\.){3}\\d{1,3}))" +
        "(\\:\\d+)?(/[-a-z\\d%_.~+]*)*" +
        "(\\?[;&a-z\\d%_.~+=-]*)?" +
        "(\\#[-a-z\\d_]*)?\$"
      ).toRegex(RegexOption.IGNORE_CASE)

    return urlRegex.matches(url)
  }

  fun getRelativeTimeSpanString(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
      timestamp,
      Clock.System.now().toEpochMilliseconds(),
      DateUtils.MINUTE_IN_MILLIS,
      DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
  }
}
