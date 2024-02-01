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

package com.github.whitescent.mastify.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.github.whitescent.mastify.paging.PageLoadState.Error
import com.github.whitescent.mastify.paging.PageLoadState.NotLoading

@Immutable
data class PaginatorUiState(
  val loadState: PageLoadState,
  val endReached: Boolean,
) {
  val canPaging get() = (loadState is Error || loadState is NotLoading) && !endReached
}

@Composable
fun rememberPaginatorUiState(
  paginator: Paginator
) = remember(paginator.pagingLoadState) {
  PaginatorUiState(
    loadState = paginator.pagingLoadState,
    endReached = (paginator.pagingLoadState as? NotLoading)?.endReached == true
  )
}
