package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.Continue
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.End
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.Start
import com.github.whitescent.mastify.network.model.status.isReplyTo
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.other.StatusDetailNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val api: MastodonApi
) : ViewModel() {

  private var isInitialLoad = false

  val navArgs: StatusDetailNavArgs = savedStateHandle.navArgs()

  private val _replyText = MutableStateFlow("")
  val replyText = _replyText.asStateFlow()

  var uiState by mutableStateOf(StatusDetailUiState())
    private set

  fun favoriteStatus(id: String) = viewModelScope.launch {
    api.favouriteStatus(id)
  }

  fun unfavoriteStatus(id: String) = viewModelScope.launch {
    api.unfavouriteStatus(id)
  }

  init {
    uiState = uiState.copy(loading = true)
    viewModelScope.launch {
      api.statusContext(navArgs.status.actionableId).fold(
        {
          uiState = uiState.copy(
            loading = false,
            ancestors = markAncestors(it.ancestors),
            descendants = markDescendants(it.descendants)
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

  fun updateText(text: String) = _replyText.update { text }

  private fun markAncestors(ancestors: List<Status>): ImmutableList<StatusUiData> {
    if (ancestors.isEmpty()) return persistentListOf()
    val result = ancestors.toMutableList().also {
      it[0] = it[0].copy(replyChainType = Start)
    }
    ancestors.forEachIndexed { index, status ->
      when (index) {
        in 1..ancestors.lastIndex -> result[index] = status.copy(replyChainType = Continue)
      }
    }
    return result.toUiData().toImmutableList()
  }

  private fun markDescendants(descendants: List<Status>): ImmutableList<StatusUiData> {
    if (descendants.isEmpty() || descendants.size == 1)
      return descendants.toUiData().toImmutableList()

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

    fun markStatus(statusList: List<Status>): List<Status> {
      val result = mutableListOf<Status>()
      statusList.forEachIndexed { index, current ->
        val next = statusList.getOrNull(index + 1)
        val prev = statusList.getOrNull(index - 1)

        val isFirst = index == 0
        val isLast = index == statusList.lastIndex

        fun replaceReplyType(new: ReplyChainType) =
          result.add(index, current.copy(replyChainType = new))

        if (isFirst && next.isReplyTo(current)) replaceReplyType(Start)

        // End of reply chain: Last in thread, replying to previous
        else if (isLast && current.isReplyTo(prev)) replaceReplyType(End)

        // Other cases:
        else if (!isFirst && !isLast) when {
          // Continue of reply chain: Replying to previous and followed by a reply to this
          current.isReplyTo(prev) && next.isReplyTo(current) -> replaceReplyType(Continue)

          // End of reply chain: Replying to previous and not followed by a reply to this
          current.isReplyTo(prev) && !next.isReplyTo(current) -> replaceReplyType(End)

          // Start of reply chain: Not replying to previous and followed by a reply to this
          !current.isReplyTo(prev) && next.isReplyTo(current) -> replaceReplyType(Start)
        }
      }
      return result
    }

    replyList.forEach { current ->
      val subReplies = searchSubReplies(current.id).toMutableList()
      if (subReplies.isNotEmpty()) {
        subReplies.add(0, current)
        finalList.addAll(markStatus(subReplies))
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
  val loadError: Boolean = false
)
