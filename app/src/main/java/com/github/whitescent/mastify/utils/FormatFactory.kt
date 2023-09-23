/*
 * Copyright 2023 WhiteScent
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
