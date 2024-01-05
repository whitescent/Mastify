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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.whitescent.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.math.abs

private const val SECOND_IN_MILLIS: Long = 1000
private const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60
private const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60
private const val DAY_IN_MILLIS = HOUR_IN_MILLIS * 24
private const val YEAR_IN_MILLIS = DAY_IN_MILLIS * 365

/**
 * Calculate how long the given timestamp is from now
 */
@Composable
fun formatDurationUntilEnd(timestamp: String): String {
  val apiInstant = Instant.parse(timestamp)
  val nowInstant = Clock.System.now()
  val duration = apiInstant - nowInstant

  if (duration.isNegative()) return stringResource(id = R.string.vote_poll_expired)

  val totalSeconds = duration.inWholeSeconds
  val days = totalSeconds / 86400
  val hours = (totalSeconds % 86400) / 3600
  val minutes = (totalSeconds % 3600) / 60

  return when {
    days > 0 -> stringResource(R.string.vote_poll_day, days)
    hours > 0 -> stringResource(R.string.vote_poll_hours, hours)
    minutes > 0 -> stringResource(R.string.vote_poll_minutes, minutes)
    else -> stringResource(R.string.vote_poll_soon_ending)
  }
}

/**
 * This is a rough duplicate of [android.text.format.DateUtils.getRelativeTimeSpanString],
 * but even with the FORMAT_ABBREV_RELATIVE flag it wasn't abbreviating enough.
 */
fun getRelativeTimeSpanString(context: Context, then: Long, now: Long): String {
  var span = now - then
  var future = false
  if (abs(span) < SECOND_IN_MILLIS) {
    return context.getString(R.string.status_created_at_now)
  } else if (span < 0) {
    future = true
    span = -span
  }
  val format: Int
  if (span < MINUTE_IN_MILLIS) {
    span /= SECOND_IN_MILLIS
    format = if (future) {
      R.string.abbreviated_in_seconds
    } else {
      R.string.abbreviated_seconds_ago
    }
  } else if (span < HOUR_IN_MILLIS) {
    span /= MINUTE_IN_MILLIS
    format = if (future) {
      R.string.abbreviated_in_minutes
    } else {
      R.string.abbreviated_minutes_ago
    }
  } else if (span < DAY_IN_MILLIS) {
    span /= HOUR_IN_MILLIS
    format = if (future) {
      R.string.abbreviated_in_hours
    } else {
      R.string.abbreviated_hours_ago
    }
  } else if (span < YEAR_IN_MILLIS) {
    span /= DAY_IN_MILLIS
    format = if (future) {
      R.string.abbreviated_in_days
    } else {
      R.string.abbreviated_days_ago
    }
  } else {
    span /= YEAR_IN_MILLIS
    format = if (future) {
      R.string.abbreviated_in_years
    } else {
      R.string.abbreviated_years_ago
    }
  }
  return context.getString(format, span)
}
