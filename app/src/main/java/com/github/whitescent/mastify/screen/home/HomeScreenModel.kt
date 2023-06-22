package com.github.whitescent.mastify.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.paging.TimelineRemoteMediator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenModel @Inject constructor(
  db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val api: MastodonApi,
  private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

  private val timelineDao = db.timelineDao()

  var activeAccount by mutableStateOf(accountRepository.activeAccount!!)
  var refreshing by mutableStateOf(false)

  val accounts = accountRepository.accounts
  val timelineScrollPosition = preferenceRepository.timelineModel?.firstVisibleItemIndex
  val timelineScrollPositionOffset = preferenceRepository.timelineModel?.offset

  init {
    viewModelScope.launch {
      // fetch the latest account info
      api.accountVerifyCredentials(activeAccount.domain, "Bearer ${activeAccount.accessToken}")
        .fold(
          {
            accountRepository.updateActiveAccount(it)
            activeAccount = accountRepository.activeAccount!!
          },
          {

          }
        )
    }
  }

  @OptIn(ExperimentalPagingApi::class)
  val pager = Pager(
    config = PagingConfig(
      pageSize = 20
    ),
    remoteMediator = TimelineRemoteMediator(accountRepository, db, api)
  ) {
    timelineDao.getStatuses(activeAccount.id)
  }.flow.cachedIn(viewModelScope)

  fun changeActiveAccount(accountId: Long) =
    accountRepository.setActiveAccount(accountId)

  fun saveTimelineScrollPosition(index: Int, offset: Int) =
    preferenceRepository.saveTimelineScrollPosition(index, offset)

  fun favoriteStatus(id: String) = viewModelScope.launch {
    api.favouriteStatus(id)
  }

  fun unfavoriteStatus(id: String) = viewModelScope.launch {
    api.unfavouriteStatus(id)
  }

}
