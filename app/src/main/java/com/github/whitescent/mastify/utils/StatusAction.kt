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

import androidx.compose.runtime.Stable
import com.github.whitescent.mastify.network.model.status.Status

@Stable
sealed interface StatusAction {
  data class CopyText(val text: String) : StatusAction
  data class CopyLink(val link: String) : StatusAction
  data class Favorite(val id: String, val favorite: Boolean) : StatusAction
  data class Bookmark(val id: String, val bookmark: Boolean) : StatusAction
  data class Reblog(val id: String, val reblog: Boolean) : StatusAction
  data class VotePoll(val id: String, val choices: List<Int>, val originStatus: Status) : StatusAction
  data object Mute : StatusAction // TODO
  data object Block : StatusAction // TODO
  data object Report : StatusAction // TODO
}
