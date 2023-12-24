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

package com.github.whitescent.mastify.extensions

import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusUiData

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

fun List<StatusUiData>.updateStatusActionData(newStatus: StatusBackResult): List<StatusUiData> {
  return if (this.any { it.actionableId == newStatus.id }) {
    this.toMutableList().map {
      if (it.actionableId == newStatus.id) {
        it.copy(
          favorited = newStatus.favorited,
          favouritesCount = newStatus.favouritesCount,
          reblogged = newStatus.reblogged,
          reblogsCount = newStatus.reblogsCount,
          repliesCount = newStatus.repliesCount,
          bookmarked = newStatus.bookmarked,
          actionable = it.actionable.copy(
            favorited = newStatus.favorited,
            favouritesCount = newStatus.favouritesCount,
            reblogged = newStatus.reblogged,
            reblogsCount = newStatus.reblogsCount,
            repliesCount = newStatus.repliesCount,
            bookmarked = newStatus.bookmarked,
          )
        )
      } else it
    }
  } else this
}
