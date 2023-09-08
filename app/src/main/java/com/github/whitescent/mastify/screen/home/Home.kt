package com.github.whitescent.mastify.screen.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.getReplyChainType
import com.github.whitescent.mastify.data.model.ui.hasUnloadedParent
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.screen.destinations.PostDestination
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.StatusEndIndicator
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.StatusSnackBarType
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.AppTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.viewModel.HomeViewModel
import com.github.whitescent.mastify.viewModel.StatusAction
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@AppNavGraph(start = true)
@Destination(style = AppTransitions::class)
@Composable
fun Home(
  appState: AppState,
  drawerState: DrawerState,
  lazyState: LazyListState,
  navigator: DestinationsNavigator,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val timeline by viewModel.timelineList.collectAsStateWithLifecycle(listOf())
  val firstVisibleIndex by remember {
    derivedStateOf {
      lazyState.firstVisibleItemIndex
    }
  }
  var refreshing by remember { mutableStateOf(false) }
  var showSnackBar by remember { mutableStateOf(false) }
  var snackBarType by remember { mutableStateOf(StatusSnackBarType.TEXT) }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val uiState = viewModel.uiState

  val pullRefreshState = rememberPullRefreshState(
    refreshing = refreshing,
    onRefresh = {
      scope.launch {
        refreshing = true
        delay(500)
        viewModel.refreshTimeline()
        refreshing = false
      }
    },
  )
  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(bottom = appState.appPaddingValues.calculateBottomPadding())
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
      HorizontalDivider(thickness = 0.5.dp, color = AppTheme.colors.divider)
      when (timeline.size) {
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
              itemsIndexed(
                items = timeline,
                contentType = { _, item -> item.itemType },
                key = { _, item -> item.id }
              ) { index, status ->
                val replyChainType by remember(status, timeline.size, index) {
                  mutableStateOf(timeline.getReplyChainType(index))
                }
                val hasUnloadedParent by remember(status, timeline.size, index) {
                  mutableStateOf(timeline.hasUnloadedParent(index))
                }
                StatusListItem(
                  status = status,
                  replyChainType = replyChainType,
                  hasUnloadedParent = hasUnloadedParent,
                  action = {
                    viewModel.onStatusAction(it, context)
                    if (it.canShowSnackBar) { // There may be a better approach here
                      snackBarType = when (it) {
                        is StatusAction.CopyLink -> StatusSnackBarType.LINK
                        is StatusAction.Bookmark -> StatusSnackBarType.BOOKMARK
                        else -> StatusSnackBarType.TEXT
                      }
                      showSnackBar = true
                    }
                  },
                  navigateToDetail = {
                    navigator.navigate(
                      StatusDetailDestination(
                        avatar = viewModel.activeAccount.profilePictureUrl,
                        status = status.actionable
                      )
                    )
                  },
                  navigateToMedia = { attachments, targetIndex ->
                    navigator.navigate(
                      StatusMediaScreenDestination(attachments.toTypedArray(), targetIndex)
                    )
                  },
                  navigateToProfile = {
                    navigator.navigate(
                      ProfileDestination(it)
                    )
                  },
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                )
                if (replyChainType == End || replyChainType == Null) AppHorizontalDivider()
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
                        color = AppTheme.colors.primaryContent,
                        strokeWidth = 2.dp
                      )
                    }
                  }
                  LoadState.Error -> {
                    Toast.makeText(context, "获取嘟文失败，请稍后重试", Toast.LENGTH_SHORT).show()
                    viewModel.append() // retry
                  }
                  else -> Unit
                }
                if (uiState.endReached) StatusEndIndicator(Modifier.padding(36.dp))
              }
            }
            NewStatusToast(
              visible = uiState.showNewStatusButton,
              count = uiState.newStatusCount,
              modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
            ) {
              scope.launch {
                lazyState.scrollToItem(0)
                viewModel.dismissButton()
              }
            }
            Column(Modifier.align(Alignment.BottomEnd)) {
              Image(
                painter = painterResource(id = R.drawable.edit),
                contentDescription = null,
                modifier = Modifier
                  .padding(24.dp)
                  .align(Alignment.End)
                  .background(AppTheme.colors.primaryGradient, CircleShape)
                  .shadow(6.dp, CircleShape)
                  .clickable {
                    navigator.navigate(PostDestination)
                  }
                  .padding(16.dp)
              )
              StatusSnackBar(
                show = showSnackBar,
                snackBarType = snackBarType,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 36.dp)
              ) { showSnackBar = false }
            }
          }
        }
      }
    }
    PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
  }
  LaunchedEffect(firstVisibleIndex) {
    if (firstVisibleIndex == 0 && uiState.showNewStatusButton) viewModel.dismissButton()
    snapshotFlow { firstVisibleIndex }
      .map {
        !uiState.endReached && uiState.timelineLoadState == LoadState.NotLoading &&
          lazyState.firstVisibleItemIndex >= timeline.size - timeline.size / 3
      }
      .filter { it }
      .collect {
        viewModel.append()
      }
  }
}

@Composable
private fun NewStatusToast(
  visible: Boolean,
  count: String,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit,
) {
  AnimatedVisibility(
    visible = visible,
    enter = scaleIn() + fadeIn(tween(400)),
    exit = scaleOut() + fadeOut(tween(400)),
    modifier = modifier
  ) {
    Surface(
      shape = CircleShape,
      color = AppTheme.colors.accent,
      shadowElevation = 4.dp
    ) {
      CenterRow(
        modifier = Modifier
          .clickable { onDismiss() }
          .padding(horizontal = 18.dp, vertical = 8.dp),
      ) {
        Icon(
          painter = painterResource(id = R.drawable.arrow_up),
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = "$count 条新嘟文",
          fontSize = 16.sp,
          color = Color.White,
          fontWeight = FontWeight(500)
        )
      }
    }
  }
}

@Composable
private fun LoadMorePlaceHolder(loadMore: () -> Unit) {
  var loading by remember { mutableStateOf(false) }
  Column {
    Surface(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .height(56.dp),
      shape = RoundedCornerShape(18.dp),
      color = Color(0xFFebf4fb)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable {
            loading = true
            loadMore()
          }
          .padding(12.dp),
        contentAlignment = Alignment.Center
      ) {
        Crossfade(loading) {
          when (it) {
            false -> {
              Text(
                text = "加载更多",
                color = AppTheme.colors.hintText,
                fontSize = 16.sp
              )
            }
            true -> CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = AppTheme.colors.primaryContent
            )
          }
        }
      }
    }
    HeightSpacer(value = 12.dp)
  }
}

@Composable
private fun EmptyTimeline() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center
  ) { Text("你似乎还没关注其他人哦", fontSize = 18.sp) }
}

@Composable
private fun Loading() {
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
private fun Error(retry: () -> Unit) {
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
