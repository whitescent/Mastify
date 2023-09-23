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

import androidx.lifecycle.ViewModel
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
  private val accountRepository: AccountRepository,
  private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

  val activeAccount get() = accountRepository.activeAccount
  val accounts get() = accountRepository.accounts

  val timelineScrollPosition = preferenceRepository.timelineModel?.firstVisibleItemIndex
  val timelineScrollPositionOffset = preferenceRepository.timelineModel?.offset

  fun changeActiveAccount(accountId: Long) =
    accountRepository.setActiveAccount(accountId)

  fun saveTimelineScrollPosition(index: Int, offset: Int) =
    preferenceRepository.saveTimelineScrollPosition(index, offset)
}
