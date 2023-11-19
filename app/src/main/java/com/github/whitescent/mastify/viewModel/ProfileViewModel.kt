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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.paging.ProfilePagingSource
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.profile.ProfileNavArgs
import com.github.whitescent.mastify.utils.StatusAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val profilePagerSize = 20

@HiltViewModel
class ProfileViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  db: AppDatabase,
  private val statusActionHandler: StatusActionHandler,
  private val api: MastodonApi,
) : ViewModel() {

  private val navArgs: ProfileNavArgs = savedStateHandle.navArgs()
  private val accountDao = db.accountDao()

  val snackBarFlow = statusActionHandler.snackBarFlow

  var uiState by mutableStateOf(ProfileUiState(account = navArgs.account))
    private set

  val statusPager = Pager(
    config = PagingConfig(
      pageSize = profilePagerSize,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = {
      ProfilePagingSource(
        onlyMedia = false,
        excludeReplies = true,
        api = api,
        accountId = uiState.account.id
      )
    },
  ).flow.cachedIn(viewModelScope)

  val statusWithReplyPager = Pager(
    config = PagingConfig(
      pageSize = profilePagerSize,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = {
      ProfilePagingSource(excludeReplies = false, api = api, accountId = uiState.account.id)
    },
  ).flow.cachedIn(viewModelScope)

  val statusWithMediaPager = Pager(
    config = PagingConfig(
      pageSize = profilePagerSize,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = {
      ProfilePagingSource(onlyMedia = true, api = api, accountId = uiState.account.id)
    },
  ).flow.cachedIn(viewModelScope)

  init {
    viewModelScope.launch {
      getRelationship(navArgs.account.id)
      fetchAccount(navArgs.account.id)
    }
  }

  fun onStatusAction(action: StatusAction, context: Context) = viewModelScope.launch {
    statusActionHandler.onStatusAction(action, context)
  }

  private suspend fun fetchAccount(accountId: String) {
    api.account(accountId).fold(
      {
        uiState = uiState.copy(account = it)
      },
      {
        it.printStackTrace()
      }
    )
  }

  private suspend fun getRelationship(accountId: String) {
    api.relationships(listOf(accountId)).fold(
      {
        uiState = uiState.copy(
          isSelf = navArgs.account.id == accountDao.getActiveAccount()!!.accountId,
          isFollowing = it.first().following
        )
      },
      {
        it.printStackTrace()
      }
    )
  }
}

data class ProfileUiState(
  val account: Account,
  val isSelf: Boolean? = null,
  val isFollowing: Boolean? = null,
)
