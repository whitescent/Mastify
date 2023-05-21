package com.github.whitescent.mastify.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.paging.HomeTimelinePagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenModel @Inject constructor(
  private val apiRepository: ApiRepository,
  private val preferenceRepository: PreferenceRepository
) : ViewModel() {

  val account = preferenceRepository.account!!

  val pager = Pager(
    config = PagingConfig(
      pageSize = 1,
      enablePlaceholders = true
    ),
    pagingSourceFactory = { HomeTimelinePagingSource(apiRepository, preferenceRepository) }
  ).flow.cachedIn(viewModelScope)
  
}
