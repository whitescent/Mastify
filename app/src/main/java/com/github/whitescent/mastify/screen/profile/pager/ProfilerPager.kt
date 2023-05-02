package com.github.whitescent.mastify.screen.profile.pager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import com.github.whitescent.mastify.network.model.response.account.Profile
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.screen.profile.pager.about.AboutScreen
import com.github.whitescent.mastify.ui.component.status.StatusList
import com.github.whitescent.mastify.ui.component.CenterCircularProgressIndicator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePager(
  state: PagerState,
  statuses: List<Status>,
  aboutModel: Profile?
) {
  HorizontalPager(
    pageCount = 2,
    state = state
  ) { page ->
    Crossfade(targetState = page) {
      when (it) {
        0 -> {
          Crossfade(targetState = statuses) { list ->
            when (list.size) {
              0 -> CenterCircularProgressIndicator()
              else -> StatusList(statuses = statuses)
            }
          }
        }
        1 -> AboutScreen(aboutModel = aboutModel)
      }
    }
  }
}
