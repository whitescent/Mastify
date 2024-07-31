/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.extensions.getReplyChainType
import com.github.whitescent.mastify.extensions.hasUnloadedParent
import com.github.whitescent.mastify.extensions.observeWithLifecycle
import com.github.whitescent.mastify.paging.LazyPagingList
import com.github.whitescent.mastify.screen.destinations.PostDestination
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.screen.destinations.TagInfoDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.BottomBarScreenTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.viewModel.HomeViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AppNavGraph(start = true)
@Destination(style = BottomBarScreenTransitions::class)
@Composable
fun Home(
  appState: AppState,
  drawerState: DrawerState,
  navigator: DestinationsNavigator,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val data by viewModel.homeCombinedFlow.collectAsStateWithLifecycle()
  val uiState = viewModel.uiState

  var refreshing by remember { mutableStateOf(false) }

  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()

  val pullRefreshState = rememberPullRefreshState(
    refreshing = refreshing,
    onRefresh = {
      scope.launch {
        refreshing = true
        delay(500)
        viewModel.refreshTimeline()
        refreshing = false
      }
    }
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .statusBarsPadding()
      .padding(bottom = appState.appPaddingValues.calculateBottomPadding())
      .pullRefresh(pullRefreshState)
      .semantics {
        testTagsAsResourceId = true
      }
  ) {
    data?.let { homeData ->
      val timeline = homeData.timeline
      val timelinePosition = homeData.position
      val activeAccount = homeData.activeAccount

      val lazyState = rememberSaveable(activeAccount.id, saver = LazyListState.Saver) {
        LazyListState(timelinePosition.index, timelinePosition.offset)
      }

      val firstVisibleIndex by remember(lazyState) {
        derivedStateOf {
          lazyState.firstVisibleItemIndex
        }
      }

      val atTop by remember(lazyState) {
        derivedStateOf {
          lazyState.firstVisibleItemIndex == 0
        }
      }
      Column {
        HomeTopBar(
          avatar = activeAccount.profilePictureUrl,
          openDrawer = {
            scope.launch {
              drawerState.open()
            }
          },
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
        HorizontalDivider(thickness = 0.5.dp, color = AppTheme.colors.divider)
        Box {
          LazyPagingList(
            paginator = viewModel.paginator,
            lazyListState = lazyState,
            pagePlaceholderType = PagePlaceholderType.Home,
            list = timeline,
            modifier = Modifier
              .fillMaxSize()
              .drawVerticalScrollbar(lazyState)
              .semantics {
                testTag = "home timeline"
              }
          ) {
            itemsIndexed(
              items = timeline,
              contentType = { _, _ -> StatusUiData },
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
                  viewModel.onStatusAction(it, status.actionable)
                },
                navigateToDetail = {
                  navigator.navigate(
                    StatusDetailDestination(
                      status = status.actionable,
                      originStatusId = status.id
                    )
                  )
                },
                navigateToMedia = { attachments, targetIndex ->
                  navigator.navigate(
                    StatusMediaScreenDestination(attachments.toTypedArray(), targetIndex)
                  )
                },
                navigateToTagInfo = {
                  navigator.navigate(TagInfoDestination(it))
                },
                navigateToProfile = {
                  navigator.navigate(ProfileDestination(it))
                }
              )
              if (!status.hasUnloadedStatus && (replyChainType == End || replyChainType == Null))
                AppHorizontalDivider()
              if (status.hasUnloadedStatus)
                LoadMorePlaceHolder(viewModel.loadMoreState) {
                  viewModel.loadUnloadedStatus(status.id)
                }
            }
          }
          NewStatusToast(
            visible = uiState.toastButton.showNewToastButton,
            count = uiState.toastButton.newStatusCount,
            limitExceeded = uiState.toastButton.showManyPost,
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 12.dp)
          ) {
            scope.launch {
              lazyState.scrollToItem(0)
              viewModel.dismissButton()
            }
          }
          Column(Modifier.align(Alignment.BottomEnd).animateContentSize()) {
            Image(
              painter = painterResource(id = R.drawable.edit),
              contentDescription = null,
              modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End)
                .shadow(6.dp, CircleShape)
                .background(AppTheme.colors.primaryGradient, CircleShape)
                .clickable { navigator.navigate(PostDestination) }
                .padding(16.dp)
            )
            StatusSnackBar(
              snackbarState = snackbarState,
              modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 36.dp)
            )
          }
        }
      }

      PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))

      appState.scrollToTopFlow.observeWithLifecycle {
        lazyState.scrollToItem(0)
      }

      LaunchedEffect(activeAccount.id) {
        launch {
          snapshotFlow { firstVisibleIndex }
            .debounce(500L)
            .collectLatest {
              viewModel.updateTimelinePosition(it, lazyState.firstVisibleItemScrollOffset)
            }
        }
        launch {
          viewModel.snackBarFlow.collect {
            snackbarState.show(it)
          }
        }
      }
      LaunchedEffect(atTop) {
        if (atTop && uiState.toastButton.showNewToastButton) viewModel.dismissButton()
      }
    }
  }
}

@Composable
private fun NewStatusToast(
  visible: Boolean,
  count: Int,
  limitExceeded: Boolean,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit,
) {
  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically { -it * 4 } + fadeIn(),
    exit = slideOutVertically { -it * 4 } + fadeOut(),
    modifier = modifier
  ) {
    Surface(
      shape = CircleShape,
      color = AppTheme.colors.accent,
      shadowElevation = 14.dp
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
          text = when (limitExceeded) {
            true -> stringResource(id = R.string.many_posts_title)
            else -> pluralStringResource(id = R.plurals.new_post, count, count)
          },
          fontSize = 16.sp,
          color = Color.White,
          fontWeight = FontWeight(500),
        )
      }
    }
  }
}

@Composable
private fun LoadMorePlaceHolder(
  loadState: PostState,
  loadMore: () -> Unit
) {
  Column {
    Surface(
      modifier = Modifier
        .padding(24.dp)
        .fillMaxWidth()
        .height(56.dp),
      shape = RoundedCornerShape(18.dp),
      color = Color(0xFFebf4fb)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            onClick = loadMore,
            enabled = loadState != PostState.Posting
          )
          .padding(12.dp),
        contentAlignment = Alignment.Center
      ) {
        Crossfade(loadState is PostState.Posting) {
          when (it) {
            false -> {
              Text(
                text = stringResource(id = R.string.load_more_title),
                color = AppTheme.colors.hintText,
                fontSize = 16.sp,
              )
            }
            true -> CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = AppTheme.colors.primaryContent,
              strokeWidth = 2.dp
            )
          }
        }
      }
    }
  }
}
