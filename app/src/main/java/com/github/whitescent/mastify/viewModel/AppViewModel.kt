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

package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
  db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val instanceRepository: InstanceRepository
) : ViewModel() {

  private val accountDao = db.accountDao()

  private val accountListFlow = accountDao.getAccountListFlow()
  private val activeAccountFlow = accountDao.getActiveAccountFlow().filterNotNull()

  private val changeAccountChannel = Channel<Unit>()
  val changeAccountFlow = changeAccountChannel.receiveAsFlow()

  val accountList = accountListFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val activeAccount = activeAccountFlow
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
      prepared = true
      if (isLoggedIn == true) {
        instanceRepository.upsertInstanceInfo()
        instanceRepository.upsertEmojis()
        launch {
          activeAccountFlow
            .distinctUntilChanged { old, new -> old.id == new.id }
            .collect {
              accountRepository.fetchActiveAccountAndSaveToDatabase()
            }
        }
      }
    }
  }

  fun changeActiveAccount(accountId: Long) {
    viewModelScope.launch {
      accountRepository.setActiveAccount(accountId)
      changeAccountChannel.send(Unit)
    }
  }
}
