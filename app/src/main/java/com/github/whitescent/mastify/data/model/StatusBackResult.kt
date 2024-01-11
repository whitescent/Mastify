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

package com.github.whitescent.mastify.data.model

import android.os.Parcelable
import com.github.whitescent.mastify.network.model.status.Poll
import kotlinx.parcelize.Parcelize

/**
 * Used to update the latest status action data
 * when the user returns to the status list screen from the Status Detail screen
 */
@Parcelize
data class StatusBackResult(
  val id: String,
  val favorited: Boolean,
  val favouritesCount: Int,
  val reblogged: Boolean,
  val reblogsCount: Int,
  val repliesCount: Int,
  val bookmarked: Boolean,
  val poll: Poll?
) : Parcelable
