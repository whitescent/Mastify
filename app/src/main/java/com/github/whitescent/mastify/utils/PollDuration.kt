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

import androidx.annotation.StringRes
import com.github.whitescent.R

data class PollDuration(
  val duration: Int,
  @StringRes val text: Int,
)

val pollDurationList = listOf(
  PollDuration(5 * 60, R.string.duration_5_min),
  PollDuration(30 * 60, R.string.duration_30_min),
  PollDuration(1 * 3600, R.string.duration_1_hour),
  PollDuration(6 * 3600, R.string.duration_6_hours),
  PollDuration(1 * 24 * 3600, R.string.duration_1_day),
  PollDuration(3 * 24 * 3600, R.string.duration_3_days),
  PollDuration(7 * 24 * 3600, R.string.duration_7_days),
)
