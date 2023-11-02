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

package com.github.whitescent.mastify.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.paging.TrendingPagingSource
import com.github.whitescent.mastify.utils.StatusAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
  private val db: AppDatabase,
  private val api: MastodonApi,
  private val statusActionHandler: StatusActionHandler,
  accountRepository: AccountRepository,
) : ViewModel() {

  private val activityAccount = accountRepository.activeAccount!!

  val snackBarFlow = statusActionHandler.snackBarFlow

  var uiState by mutableStateOf(ExplorerUiState())
    private set

  val trendingStatusPager = Pager(
    config = PagingConfig(
      pageSize = 20,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = {
      TrendingPagingSource(api = api)
    },
  ).flow.cachedIn(viewModelScope)

  init {
    uiState = uiState.copy(
      avatar = activityAccount.profilePictureUrl
    )
  }

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text)
  }

  fun onStatusAction(action: StatusAction, context: Context) = viewModelScope.launch {
    statusActionHandler.onStatusAction(action, context)
  }
}

data class ExplorerUiState(
  val avatar: String = "",
  val text: String = ""
)

sealed class ExplorerKind {
  data class Trending(val label: String) : ExplorerKind()
  data class News(val label: String) : ExplorerKind()
  data class Timeline(val label: String) : ExplorerKind()
}
