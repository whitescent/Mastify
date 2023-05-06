package com.github.whitescent.mastify.screen.profile.pager

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.network.model.response.account.Profile
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.screen.profile.pager.about.AboutScreen
import com.github.whitescent.mastify.ui.component.CenterCircularProgressIndicator
import com.github.whitescent.mastify.ui.component.status.StatusListItem

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
              else -> {
                LazyColumn(
                  modifier = Modifier.fillMaxSize()
                ) {
                  itemsIndexed(statuses) { _, status ->
                    StatusListItem(status)
                    Divider(modifier = Modifier.fillMaxWidth(), thickness = (0.6).dp)
                  }
                }
              }
            }
          }
        }
        1 -> AboutScreen(aboutModel = aboutModel)
      }
    }
  }
}
