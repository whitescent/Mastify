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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.whitescent.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Calculate how long the given timestamp is from now
 */
@Composable
fun formatDurationUntilEnd(timestamp: String?): String {
  if (timestamp == null) return stringResource(id = R.string.vote_no_deadline)
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
