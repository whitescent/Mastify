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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.utils.splitReorderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel @Inject constructor(
  db: AppDatabase,
  private val accountRepository: AccountRepository,
) : ViewModel() {

  private val accountDao = db.accountDao()
  private val timelineDao = db.timelineDao()

  private val accountListFlow = accountDao.getAccountListFlow()
  private val activeAccountFlow = accountDao
    .getActiveAccountFlow()
    .distinctUntilChanged { old, new -> old?.id == new?.id }
    .filterNotNull()

  private val changeAccountChannel = Channel<Unit>()
  val changeAccountFlow = changeAccountChannel.receiveAsFlow()

  val accountList = accountListFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val homeCombinedFlow = activeAccountFlow
    .flatMapLatest { account ->
      val timelineFlow = timelineDao.getStatusListWithFlow(account.id)
      timelineFlow.map {
        HomeUserData(
          activeAccount = account,
          timeline = splitReorderStatus(it).toUiData().toImmutableList(),
          position = TimelinePosition(account.firstVisibleItemIndex, account.offset)
        )
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null
    )

  var isLoggedIn by mutableStateOf<Boolean?>(null)
    private set

  var prepared by mutableStateOf(false)
    private set

  init {
    viewModelScope.launch {
      val activeAccount = accountDao.getActiveAccount()
      isLoggedIn = activeAccount != null
      if (isLoggedIn == true) {
        homeCombinedFlow.collect {
          it?.let { prepared = true }
        }
      } else prepared = true
    }
  }

  fun changeActiveAccount(accountId: Long) {
    viewModelScope.launch {
      accountRepository.setActiveAccount(accountId)
      // activeAccount.collect {
      //   if (it != null) {
      //     timelinePosition = TimelinePosition(it.firstVisibleItemIndex, it.offset)
      //     changeAccountChannel.send(Unit)
      //   }
      // }
    }
  }
}

data class HomeUserData(
  val activeAccount: AccountEntity,
  val timeline: ImmutableList<StatusUiData>,
  val position: TimelinePosition
)

data class TimelinePosition(
  val index: Int = 0,
  val offset: Int = 0
)
