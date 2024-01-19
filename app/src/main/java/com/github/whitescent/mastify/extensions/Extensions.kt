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

package com.github.whitescent.mastify.extensions

import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null

// get all items size from 0 to index
fun <A, B> Map<A, List<B>>.getSizeOfIndex(index: Int): Int {
  if (index == 0) return 0
  var count = 0
  this.onEachIndexed { currentIndex, entry ->
    count += entry.value.size
    if (currentIndex == index - 1) return count + index
  }
  return -1
}

fun String.insertString(insert: String, index: Int): String {
  val start = this.substring(0, index)
  val end = this.substring(index)
  return start + insert + end
}

/**
 * Updates the status in a status list with the latest data from the status detail screen.
 */
fun List<StatusUiData>.updateStatusActionData(newStatus: StatusBackResult): List<StatusUiData> {
  return if (this.any { it.actionableId == newStatus.id }) {
    val result = this.toMutableList()
    val index = result.indexOfFirst { it.actionableId == newStatus.id }
    if (index != -1) {
      result[index] = result[index].copy(
        favorited = newStatus.favorited,
        favouritesCount = newStatus.favouritesCount,
        reblogged = newStatus.reblogged,
        reblogsCount = newStatus.reblogsCount,
        repliesCount = newStatus.repliesCount,
        bookmarked = newStatus.bookmarked,
        poll = newStatus.poll,
        actionable = result[index].actionable.copy(
          favorited = newStatus.favorited,
          favouritesCount = newStatus.favouritesCount,
          reblogged = newStatus.reblogged,
          reblogsCount = newStatus.reblogsCount,
          repliesCount = newStatus.repliesCount,
          bookmarked = newStatus.bookmarked,
          poll = newStatus.poll,
        )
      )
      result
    } else this
  } else this
}

fun List<StatusUiData>.hasUnloadedParent(index: Int): Boolean {
  val current = get(index)
  val currentType = getReplyChainType(index)
  if (currentType == Null || !current.isInReplyTo) return false
  return when (val prev = getOrNull(index - 1)) {
    null -> false
    else -> current.inReplyToId != prev.id
  }
}

fun List<StatusUiData>.getReplyChainType(index: Int): StatusUiData.ReplyChainType {
  val prev = getOrNull(index - 1)
  val current = get(index)
  val next = getOrNull(index + 1)

  return when {
    prev != null && next != null -> {
      when {
        (current.isInReplyTo &&
          current.inReplyToId == prev.id && next.inReplyToId == current.id) -> StatusUiData.ReplyChainType.Continue
        next.inReplyToId == current.id -> StatusUiData.ReplyChainType.Start
        current.inReplyToId == prev.id -> StatusUiData.ReplyChainType.End
        else -> Null
      }
    }
    prev == null && next != null -> {
      when (next.inReplyToId) {
        current.id -> StatusUiData.ReplyChainType.Start
        else -> Null
      }
    }
    prev != null && next == null -> {
      when {
        current.isInReplyTo && current.inReplyToId == prev.id -> StatusUiData.ReplyChainType.End
        else -> Null
      }
    }
    else -> Null
  }
}
