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

package com.github.whitescent.mastify.network.model.notification

import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Report
import com.github.whitescent.mastify.network.model.status.Status
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
  val type: String,
  val id: String,
  val account: Account,
  val status: Status?,
  val report: Report?
) {

  sealed class Type {

    sealed class BasicEvent : Type()

    object Mention : BasicEvent()
    object Favourite : BasicEvent()
    object Reblog : BasicEvent()
    object Status : Type()
    object Follow : Type()
    object FollowRequest : Type()
    object Poll : Type()
    object Update : Type()
    object Unknown : Type()

    // https://docs.joinmastodon.org/methods/notifications/#get
    override fun toString(): String {
      return when (this) {
        Mention -> "mention"
        Status -> "status"
        Reblog -> "reblog"
        Follow -> "follow"
        FollowRequest -> "follow_request"
        Favourite -> "favourite"
        Poll -> "poll"
        Update -> "update"
        else -> "unknown"
      }
    }
  }

  companion object {
    fun byString(str: String): Type {
      return when (str) {
        "mention" -> Type.Mention
        "status" -> Type.Status
        "reblog" -> Type.Reblog
        "follow" -> Type.Follow
        "follow_request" -> Type.FollowRequest
        "favourite" -> Type.Favourite
        "poll" -> Type.Poll
        "update" -> Type.Update
        else -> Type.Unknown
      }
    }
  }
}
