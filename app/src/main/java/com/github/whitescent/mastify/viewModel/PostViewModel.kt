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

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.InstanceUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility
import com.github.whitescent.mastify.data.repository.FileRepository
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.data.repository.StatusRepository
import com.github.whitescent.mastify.data.repository.UploadEvent
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.utils.PostState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
  db: AppDatabase,
  private val instanceRepository: InstanceRepository,
  private val statusRepository: StatusRepository,
  private val fileRepository: FileRepository
) : ViewModel() {

  private val accountDao = db.accountDao()

  val activeAccount = accountDao
    .getActiveAccountFlow()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null
    )

  var postTextField by mutableStateOf(TextFieldValue(""))
    private set

  var uiState by mutableStateOf(PostUiState())
    private set

  val medias = mutableStateListOf<MediaModel>()

  val allowPostStatus: StateFlow<Boolean> =
    combine(
      snapshotFlow { medias.toList() },
      snapshotFlow { postTextField },
      snapshotFlow { uiState.textExceedLimit }
    ) { medias, postTextField, textExceedLimit ->
      val isMediaPrepared = !medias.any { it.ids == null }
      if (medias.isEmpty()) {
        postTextField.text.isNotEmpty() && !textExceedLimit
      } else isMediaPrepared && !textExceedLimit
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = false
    )

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
      statusRepository.createStatus(
        content = postTextField.text,
        mediaIds = medias.map { it.ids!! },
        visibility = uiState.visibility,
      ).fold(
        { _ ->
          uiState = uiState.copy(postState = PostState.Success)
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(postState = PostState.Failure(it))
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
      textExceedLimit = postTextField.text.length > uiState.instanceUiData.maximumTootCharacters
    )
  }

  fun addMedia(uris: List<Uri>) {
    if (uris.isEmpty()) return
    val newList = uris
      .filter { uri -> !medias.any { it.uri == uri } }
      .map { MediaModel(it) }
      .reversed()
    medias.addAll(newList)
    newList.forEach { mediaModel ->
      viewModelScope.launch {
        fileRepository.addMediaToQueue(mediaModel.uri!!).collect {
          val index = medias.indexOfFirst { item -> item.uri == mediaModel.uri }
          if (index != -1) {
            medias[index] = medias[index].copy(
              uploadEvent = it,
              ids = (it as? UploadEvent.FinishedEvent)?.mediaId
            )
          }
        }
      }
    }
  }

  fun removeMedia(uri: Uri?) {
    fileRepository.cancelUpload(uri)
    medias.removeIf { media -> media.uri == uri }
  }
}

data class MediaModel(
  val uri: Uri? = null,
  val ids: String? = null,
  val uploadEvent: UploadEvent = UploadEvent.ProgressEvent(0)
)

data class PostUiState(
  val instanceUiData: InstanceUiData = InstanceUiData(),
  val emojis: ImmutableList<Emoji> = persistentListOf(),
  val postState: PostState = PostState.Idle,
  val visibility: Visibility = Visibility.Public,
  val textExceedLimit: Boolean = false,
)
