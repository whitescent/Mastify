package com.github.whitescent.mastify.screen.home

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.BottomBarNavGraph
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, FlowPreview::class)
@BottomBarNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
  lazyState: LazyListState,
  topNavController: NavController,
  viewModel: HomeScreenModel
) {
  val homeTimeline = viewModel.pager.collectAsLazyPagingItems()
  val context = LocalContext.current

  var refreshing by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()
  val state = rememberPullRefreshState(
    refreshing = refreshing,
    onRefresh = {
      scope.launch {
        refreshing = true
        delay(500)
        homeTimeline.refresh()
        refreshing = false
      }
    },
  )
  Box(
    Modifier
      .statusBarsPadding()
      .pullRefresh(state)
  ) {
    Column {
      HomeScreenTopBar(avatar = viewModel.account.profilePictureUrl)
      when (homeTimeline.itemCount) {
        0 -> {
          when (homeTimeline.loadState.refresh) {
            is LoadState.Error -> Error { homeTimeline.retry() }
            else -> Loading()
          }
        }
        else -> {
          Box {
            LazyColumn(
              state = lazyState,
              modifier = Modifier
                .fillMaxSize()
                .drawVerticalScrollbar(lazyState),
              verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
              items(
                count = homeTimeline.itemCount,
                key = homeTimeline.itemKey(),
                contentType = homeTimeline.itemContentType()
              ) { index ->
                val item = homeTimeline[index]
                item?.let { status ->
                  StatusListItem(status = status)
                }
              }
              item {
                when (homeTimeline.loadState.append) {
                  is LoadState.Loading -> {
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
                  is LoadState.NotLoading -> {
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
                  is LoadState.Error -> {
                    Toast.makeText(context, "获取嘟文失败，请稍后重试", Toast.LENGTH_SHORT).show()
                    homeTimeline.retry()
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
    PullRefreshIndicator(refreshing, state, Modifier.align(Alignment.TopCenter))
  }
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
//        style = AppTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
      HeightSpacer(value = 4.dp)
      Button(
        onClick = retry,
//        shape = AppTheme.shapes.medium
      ) {
        Text(
          text = "重新获取",
        )
      }
    }
  }
}
