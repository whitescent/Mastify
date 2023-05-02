package com.github.whitescent.mastify.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.CenterCircularProgressIndicator
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar

@Composable
fun HomeScreen(
  mainNavController: NavController,
  viewModel: HomeScreenModel = hiltViewModel()
) {

  val homeTime = viewModel.pager.collectAsLazyPagingItems()
  val lazyState = rememberLazyListState()

  when (homeTime.loadState.refresh) {
    is LoadState.NotLoading -> {
      LazyColumn(
        state = lazyState,
        modifier = Modifier
          .statusBarsPadding()
          .fillMaxSize()
          .drawVerticalScrollbar(lazyState)
      ) {
        items(homeTime) { status ->
          status?.let {
            key(it.id) {
              StatusListItem(status = status)
            }
          }
        }
      }
    }
    is LoadState.Loading -> CenterCircularProgressIndicator()
    is LoadState.Error -> Unit
    else -> Unit
  }
}
