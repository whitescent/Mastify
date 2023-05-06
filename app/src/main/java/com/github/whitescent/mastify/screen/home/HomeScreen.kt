package com.github.whitescent.mastify.screen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.github.whitescent.mastify.BottomBarNavGraph
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.CenterCircularProgressIndicator
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.ramcosta.composedestinations.annotation.Destination

@BottomBarNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
  lazyState: LazyListState,
  navController: NavController,
  viewModel: HomeScreenModel = hiltViewModel()
) {

  val homeTimeline = viewModel.pager.collectAsLazyPagingItems()
  val context = LocalContext.current

  when (homeTimeline.loadState.refresh) {
    is LoadState.NotLoading -> {
      LazyColumn(
        state = lazyState,
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .background(Color.White)
          .drawVerticalScrollbar(lazyState)
      ) {
        items(homeTimeline) { status ->
          status?.let {
            key(it.id) {
              StatusListItem(status = status)
            }
          }
        }
        item {
          when (homeTimeline.loadState.append) {
            is LoadState.Loading -> {
              Box(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            is LoadState.NotLoading -> {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(24.dp),
                contentAlignment = Alignment.Center
              ) { Box(Modifier.size(8.dp).background(Color.Gray, CircleShape)) }
            }
            is LoadState.Error -> {
              Toast.makeText(context, "获取嘟文失败，请稍后重试", Toast.LENGTH_SHORT).show()
              homeTimeline.retry()
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
