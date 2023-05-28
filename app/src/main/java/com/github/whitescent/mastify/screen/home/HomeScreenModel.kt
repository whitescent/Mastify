package com.github.whitescent.mastify.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.database.MastifyDatabase
import com.github.whitescent.mastify.paging.TimelineRemoteMediator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenModel @Inject constructor(
  db: MastifyDatabase,
  api: ApiRepository,
  private val preferenceRepository: PreferenceRepository
) : ViewModel() {

  private val timelineDao = db.timelineDao()

  val account = preferenceRepository.account!!
  val timelineScrollPosition = preferenceRepository.timelineScrollPosition

  @OptIn(ExperimentalPagingApi::class)
  val pager = Pager(
    config = PagingConfig(
      pageSize = 2
    ),
    remoteMediator = TimelineRemoteMediator(db, api, preferenceRepository)
  ) {
    timelineDao.getStatuses()
  }.flow.cachedIn(viewModelScope)

  fun saveTimelineScrollPosition(index: Int) {
    preferenceRepository.saveTimelineScrollPosition(index)
  }

}
