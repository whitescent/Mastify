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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.InstanceUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.NewStatus
import com.github.whitescent.mastify.utils.PostState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
  private val accountRepository: AccountRepository,
  private val instanceRepository: InstanceRepository,
  private val api: MastodonApi
) : ViewModel() {

  val account get() = accountRepository.activeAccount

  var postTextField by mutableStateOf(TextFieldValue(""))
    private set

  var uiState by mutableStateOf(PostUiState())
    private set

  init {
    viewModelScope.launch {
      uiState = uiState.copy(
        instanceUiData = instanceRepository.getAndUpdateInstanceInfo(),
        emojis = instanceRepository.getEmojis().toImmutableList()
      )
    }
  }

  fun postStatus() {
    uiState = uiState.copy(postState = PostState.Posting)
    viewModelScope.launch {
      api.createStatus(
        idempotencyKey = UUID.randomUUID().toString(),
        status = NewStatus(
          status = postTextField.text,
          warningText = "",
          inReplyToId = null,
          visibility = uiState.visibility.toString(),
          sensitive = false, // TODO
          mediaIds = null,
          mediaAttributes = null,
          scheduledAt = null,
          poll = null,
          language = null,
        ),
      ).fold(
        { status ->
          uiState = uiState.copy(postState = PostState.Success)
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(postState = PostState.Failure)
        }
      )
    }
  }

  fun updateVisibility(visibility: Visibility) {
    uiState = uiState.copy(visibility = visibility)
  }

  fun updateTextFieldValue(textFieldValue: TextFieldValue) {
    postTextField = textFieldValue
    uiState = uiState.copy(
      textExceedLimit = postTextField.text.length > uiState.instanceUiData.maximumTootCharacters!!
    )
  }
}

data class PostUiState(
  val instanceUiData: InstanceUiData = InstanceUiData(),
  val emojis: ImmutableList<Emoji> = persistentListOf(),
  val postState: PostState = PostState.Idle,
  val visibility: Visibility = Visibility.Public,
  val textExceedLimit: Boolean = false,
)
