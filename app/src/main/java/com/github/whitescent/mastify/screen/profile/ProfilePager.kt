package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.github.whitescent.mastify.data.model.ui.StatusUiData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePager(
  state: PagerState,
  accountStatus: LazyPagingItems<StatusUiData>
) {
  HorizontalPager(
    state = state,
    pageContent = {
      when (it) {
        0 -> {
          ProfileStatus(accountStatus)
        }
        1 -> Unit
        2 -> Unit
      }
    },
    modifier = Modifier.fillMaxSize()
  )
}
