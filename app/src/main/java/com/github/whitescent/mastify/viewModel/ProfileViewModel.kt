package com.github.whitescent.mastify.viewModel

import android.content.ClipData
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
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.paging.ProfilePagingSource
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.profile.ProfileNavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  accountRepository: AccountRepository,
  private val api: MastodonApi
) : ViewModel() {

  private val navArgs: ProfileNavArgs = savedStateHandle.navArgs()

  var uiState by mutableStateOf(
    ProfileUiState(
      account = navArgs.account,
      isSelf = navArgs.account.id == accountRepository.activeAccount!!.accountId
    )
  )
    private set

  val pager = Pager(
    config = PagingConfig(
      pageSize = 20,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = { ProfilePagingSource(api, this) },
  ).flow.cachedIn(viewModelScope)

  init {
    viewModelScope.launch {
      getRelationship(navArgs.account.id)
      fetchAccount(navArgs.account.id)
    }
  }

  fun onStatusAction(action: StatusAction, context: Context) {
    val clipManager =
      context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    viewModelScope.launch {
      when (action) {
        is StatusAction.Favorite -> {
          if (action.favorite) api.favouriteStatus(action.id) else api.unfavouriteStatus(action.id)
        }
        is StatusAction.Reblog -> {
          if (action.reblog) api.reblogStatus(action.id) else api.unreblogStatus(action.id)
        }
        is StatusAction.Bookmark -> {
          if (action.bookmark) api.bookmarkStatus(action.id) else api.unbookmarkStatus(action.id)
        }
        is StatusAction.CopyText -> {
          clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.text))
        }
        is StatusAction.CopyLink -> {
          clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.link))
        }
        is StatusAction.Mute -> Unit
        is StatusAction.Block -> Unit
        is StatusAction.Report -> Unit
      }
    }
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
        uiState = uiState.copy(isFollowing = it.first().following)
      },
      {
        it.printStackTrace()
      }
    )
  }
}

data class ProfileUiState(
  val account: Account,
  val isSelf: Boolean,
  val isFollowing: Boolean? = null,
)
