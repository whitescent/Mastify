package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.Continue
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.End
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.Start
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusDetailViewModel @Inject constructor(
  preferenceRepository: PreferenceRepository,
  private val db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val api: MastodonApi,
) : ViewModel() {

  private var isInitialLoad = false

  var uiState by mutableStateOf(StatusDetailUiState())
    private set

  fun favoriteStatus(id: String) = viewModelScope.launch {
    api.favouriteStatus(id)
  }

  fun unfavoriteStatus(id: String) = viewModelScope.launch {
    api.unfavouriteStatus(id)
  }

  fun loadThread(id: String) {
    if (isInitialLoad) return
    uiState = uiState.copy(loading = true)
    viewModelScope.launch {
      api.statusContext(id).fold(
        {
          uiState = if (it.descendants.isNotEmpty()) {
            uiState.copy(loading = false, thread = markThread(it.descendants))
          } else {
            uiState.copy(loading = false, isThreadEmpty = true)
          }
          isInitialLoad = true
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(loading = false, loadError = true)
        }
      )
    }
  }

  private fun markThread(thread: List<Status>): List<Status> {
    if (thread.isEmpty() || thread.size == 1) return thread
    val result = thread.toMutableList()
    thread.forEachIndexed { index, status ->
      when {
        index == 0 && thread[1].inReplyToId == status.id ||
          index > 0 && index < thread.lastIndex &&
          thread[index + 1].inReplyToId == status.id &&
          status.inReplyToId != thread[index - 1].id -> {
          result[index] = status.copy(replyChainType = Start)
        }
        index > 0 && index < thread.lastIndex &&
          status.inReplyToId == thread[index - 1].id &&
          thread[index + 1].inReplyToId == status.id -> {
          result[index] = status.copy(replyChainType = Continue)
        }
        index == thread.lastIndex && status.inReplyToId == thread[index - 1].id ||
          index > 0 && index < thread.lastIndex &&
          status.inReplyToId == thread[index - 1].id &&
          thread[index + 1].inReplyToId != status.id -> {
          result[index] = status.copy(replyChainType = End)
        }
      }
    }
    return result
  }
}

data class StatusDetailUiState(
  val loading: Boolean = false,
  val thread: List<Status> = emptyList(),
  val isThreadEmpty: Boolean = false,
  val loadError: Boolean = false
)
