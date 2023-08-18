package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.NewStatus
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.other.StatusDetailNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StatusDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val api: MastodonApi
) : ViewModel() {

  private var isInitialLoad = false

  val navArgs: StatusDetailNavArgs = savedStateHandle.navArgs()

  var replyText by mutableStateOf(TextFieldValue(""))
    private set

  var uiState by mutableStateOf(StatusDetailUiState())
    private set

  fun favoriteStatus(id: String) = viewModelScope.launch {
    api.favouriteStatus(id)
  }

  fun unfavoriteStatus(id: String) = viewModelScope.launch {
    api.unfavouriteStatus(id)
  }

  fun replyToStatus() {
    uiState = uiState.copy(postState = PostState.Posting)
    viewModelScope.launch {
      api.createStatus(
        idempotencyKey = UUID.randomUUID().toString(),
        status = NewStatus(
          status = replyText.text,
          warningText = "",
          inReplyToId = navArgs.status.actionableId,
          visibility = "public", // TODO
          sensitive = false, // TODO
          mediaIds = null,
          mediaAttributes = null,
          scheduledAt = null,
          poll = null,
          language = null
        )
      ).fold(
        {
          uiState = uiState.copy(postState = PostState.Success)
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(postState = PostState.Failure)
        }
      )
    }
  }

  init {
    uiState = uiState.copy(loading = true)
    viewModelScope.launch {
      api.statusContext(navArgs.status.actionableId).fold(
        {
          uiState = uiState.copy(
            loading = false,
            ancestors = it.ancestors.toUiData().toImmutableList(),
            descendants = reorderDescendants(it.descendants),
          )
          isInitialLoad = true
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(loading = false, loadError = true)
        }
      )
    }
  }

  fun updateText(text: TextFieldValue) { replyText = text }

  private fun reorderDescendants(descendants: List<Status>): ImmutableList<StatusUiData> {
    if (descendants.isEmpty() || descendants.size == 1)
      return descendants.toUiData().toImmutableList()

    // remove sub replies
    val replyList = descendants.filter { it.inReplyToId == navArgs.status.actionableId }
    val finalList = mutableListOf<Status>()

    fun searchSubReplies(current: String): List<Status> {
      val subReplies = mutableListOf<Status>()
      var now = current
      descendants.forEach {
        if (it.inReplyToId == now) {
          subReplies.add(it)
          now = it.id
        }
      }
      return subReplies
    }
    replyList.forEach { current ->
      val subReplies = searchSubReplies(current.id).toMutableList()
      if (subReplies.isNotEmpty()) {
        subReplies.add(0, current)
        finalList.addAll(subReplies)
      } else {
        finalList.add(current)
      }
    }
    return finalList.toUiData().toImmutableList()
  }
}

@Immutable
data class StatusDetailUiState(
  val loading: Boolean = false,
  val ancestors: ImmutableList<StatusUiData> = persistentListOf(),
  val descendants: ImmutableList<StatusUiData> = persistentListOf(),
  val loadError: Boolean = false,
  val postState: PostState = PostState.Idle
)

sealed class PostState {
  data object Idle : PostState()
  data object Posting : PostState()
  data object Success : PostState()
  data object Failure : PostState()
}
