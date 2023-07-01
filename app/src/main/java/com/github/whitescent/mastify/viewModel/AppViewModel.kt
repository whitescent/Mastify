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

  val activeAccount get () = accountRepository.activeAccount

  val accounts = accountRepository.accounts
  val timelineScrollPosition = preferenceRepository.timelineModel?.firstVisibleItemIndex
  val timelineScrollPositionOffset = preferenceRepository.timelineModel?.offset

  fun changeActiveAccount(accountId: Long) {
    accountRepository.setActiveAccount(accountId)
  }

  fun saveTimelineScrollPosition(index: Int, offset: Int) =
    preferenceRepository.saveTimelineScrollPosition(index, offset)

}
