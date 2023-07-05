package com.github.whitescent.mastify.screen.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.ui.component.status.Status
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.AppTransitions
import com.github.whitescent.mastify.viewModel.HomeViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@AppNavGraph(start = true)
@Destination(style = AppTransitions::class)
@Composable
fun Home(
  drawerState: DrawerState,
  lazyState: LazyListState,
  navigator: DestinationsNavigator,
  viewModel: HomeViewModel = hiltViewModel()
) {

  val context = LocalContext.current
  val uiState = viewModel.uiState
  val homeTimeline = uiState.statusList

  val scope = rememberCoroutineScope()
  val pullRefreshState = rememberPullRefreshState(
    refreshing = viewModel.refreshing,
    onRefresh = {
      scope.launch {
        viewModel.refreshing = true
        delay(500)
        viewModel.refreshTimeline()
        viewModel.refreshing = false
      }
    },
  )

  Box(
    Modifier
      .statusBarsPadding()
      .pullRefresh(pullRefreshState)
  ) {
    Column {
      HomeTopBar(
        avatar = viewModel.activeAccount.profilePictureUrl,
        openDrawer = {
          scope.launch {
            drawerState.open()
          }
        }
      )
      when(homeTimeline.size) {
        0 -> {
          when (uiState.timelineLoadState) {
            LoadState.Error -> Error { viewModel.refreshTimeline() }
            LoadState.NotLoading -> EmptyTimeline()
            else -> Loading()
          }
        }
        else -> {
          Box {
            LazyColumn(
              state = lazyState,
              modifier = Modifier
                .fillMaxSize()
                .drawVerticalScrollbar(lazyState)
            ) {
              items(homeTimeline) { status ->
                val loadThreshold = uiState.statusList.size - uiState.statusList.size / 4
                key(status.id) {
                  if (
                    status.id <= uiState.statusList[loadThreshold].id &&
                    !uiState.endReached && uiState.timelineLoadState == LoadState.NotLoading
                  ) {
                    viewModel.loadMore()
                  }
                  Status(
                    status = status,
                    favouriteStatus = { viewModel.favoriteStatus(status.id) },
                    unfavouriteStatus = { viewModel.unfavoriteStatus(status.id) },
                    navigateToDetail = { navigator.navigate(StatusDetailDestination) }
                  )
                  if (!status.hasReplyStatus && !status.betweenInReplyStatus) {
                    HeightSpacer(12.dp)
                  }
                }
              }
              item {
                when (uiState.timelineLoadState) {
                  LoadState.Append -> {
                    Box(
                      modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                      contentAlignment = Alignment.Center
                    ) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppTheme.colors.primaryContent
                      )
                    }
                  }
                  LoadState.Error -> {
                    Toast.makeText(context, "获取嘟文失败，请稍后重试", Toast.LENGTH_SHORT).show()
                    viewModel.loadMore() // retry
                  }
                  else -> Unit
                }
                if (uiState.endReached) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(24.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Box(
                      Modifier
                        .size(8.dp)
                        .background(Color.Gray, CircleShape))
                  }
                }
              }
            }
            Image(
              painter = painterResource(id = R.drawable.edit),
              contentDescription = null,
              modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd)
                .background(AppTheme.colors.primaryGradient, CircleShape)
                .shadow(6.dp, CircleShape)
                .clickable { }
                .padding(16.dp)
            )
          }
        }
      }
    }
    PullRefreshIndicator(viewModel.refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
  }
}

@Composable
fun EmptyTimeline() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center
  ) { Text("你似乎还没关注其他人哦", fontSize = 18.sp) }
}

@Composable
fun Loading() {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.car))
  val progress by animateLottieCompositionAsState(composition)
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    LottieAnimation(
      composition = composition,
      progress = { progress },
    )
  }
}

@Composable
fun Error(retry: () -> Unit) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error))
  val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = LottieConstants.IterateForever
  )
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(160.dp)
      )
      HeightSpacer(value = 4.dp)
      Text(
        text = "获取嘟文失败... :(",
        fontWeight = FontWeight.Bold
      )
      HeightSpacer(value = 4.dp)
      Button(
        onClick = retry,
      ) {
        Text(
          text = "重新获取",
        )
      }
    }
  }
}
