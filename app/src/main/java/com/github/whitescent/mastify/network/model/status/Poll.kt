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

package com.github.whitescent.mastify.network.model.status

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Poll(
  val id: String,
  @SerialName("expires_at") val expiresAt: String?,
  val expired: Boolean,
  val multiple: Boolean,
  @SerialName("votes_count") val votesCount: Int,
  @SerialName("voters_count") val votersCount: Int?, // nullable for compatibility with other fediverse
  val options: List<PollOption>,
  val voted: Boolean,
  @SerialName("own_votes") val ownVotes: List<Int>?
) {

  @Serializable
  data class PollOption(
    val title: String,
    @SerialName("votes_count") val votesCount: Int
  )
}
