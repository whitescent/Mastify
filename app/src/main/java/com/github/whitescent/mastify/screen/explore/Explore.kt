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

package com.github.whitescent.mastify.screen.explore

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.BottomBarScreenTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.viewModel.ExploreViewModel
import com.github.whitescent.mastify.viewModel.ExplorerKind
import com.github.whitescent.mastify.viewModel.ExplorerKind.News
import com.github.whitescent.mastify.viewModel.ExplorerKind.PublicTimeline
import com.github.whitescent.mastify.viewModel.ExplorerKind.Trending
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@AppNavGraph
@Destination(
  style = BottomBarScreenTransitions::class
)
@Composable
fun Explore(
  viewModel: ExploreViewModel = hiltViewModel(),
  appState: AppState,
  drawerState: DrawerState,
  navigator: DestinationsNavigator,
  resultRecipient: ResultRecipient<StatusDetailDestination, StatusBackResult>
) {
  val uiState = viewModel.uiState
  val context = LocalContext.current
  val density = LocalDensity.current

  val currentExploreKind by viewModel.currentExploreKind.collectAsStateWithLifecycle()
  val activeAccount by viewModel.activityAccountFlow.collectAsStateWithLifecycle()

  // when user focus on searchBar, we need hide content
  var hideTitle by remember { mutableStateOf(false) }

  val pagerState = rememberPagerState { ExplorerKind.entries.size }
  val focusRequester = remember { FocusRequester() }

  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()

  val trendingStatusListState = rememberLazyListState()
  val publicTimelineListState = rememberLazyListState()
  val newsListState = rememberLazyListState()

  val searchingResult by viewModel.searchPreviewResult.collectAsStateWithLifecycle()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .statusBarsPadding()
  ) {
    if (activeAccount != null) {
      Column {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)) {
          AnimatedVisibility(
            visible = !hideTitle,
            enter = slideInVertically {
              with(density) { -40.dp.roundToPx() }
            } + expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
          ) {
            Column {
              CenterRow {
                Text(
                  text = stringResource(id = R.string.explore_instance, activeAccount!!.domain),
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = AppTheme.colors.primaryContent,
                  modifier = Modifier.weight(1f),
                )
                CircleShapeAsyncImage(
                  model = activeAccount!!.profilePictureUrl,
                  modifier = Modifier
                    .size(36.dp)
                    .shadow(12.dp, AppTheme.shape.betweenSmallAndMediumAvatar),
                  shape = AppTheme.shape.betweenSmallAndMediumAvatar,
                  onClick = {
                    scope.launch {
                      drawerState.open()
                    }
                  }
                )
              }
              HeightSpacer(value = 6.dp)
            }
          }
          ExploreSearchBar(
            text = uiState.text,
            focusRequester = focusRequester,
            onValueChange = viewModel::onValueChange,
            clearText = viewModel::clearInputText,
            onFocusChange = { hideTitle = it }
          )
          HeightSpacer(value = 4.dp)
        }
        Crossfade(
          targetState = hideTitle,
          animationSpec = tween()
        ) {
          when (it) {
            true -> {
              ExploreSearchContent(
                isTextEmpty = uiState.text.isEmpty(),
                searchingResult = searchingResult
              )
            }
            else -> {
              Column {
                ExploreTabBar(
                  currentExploreKind = currentExploreKind,
                  modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
                ) { kind ->
                  viewModel.syncExploreKind(kind)
                  scope.launch {
                    pagerState.scrollToPage(kind)
                  }
                }
                AppHorizontalDivider(thickness = 1.dp)
                ExplorePager(
                  state = pagerState,
                  trendingStatusListState = trendingStatusListState,
                  publicTimelineListState = publicTimelineListState,
                  newsListState = newsListState,
                  viewModel = viewModel,
                  navigateToDetail = { targetStatus ->
                    navigator.navigate(
                      StatusDetailDestination(
                        status = targetStatus,
                        originStatusId = null
                      )
                    )
                  },
                  navigateToMedia = { attachments, targetIndex ->
                    navigator.navigate(
                      StatusMediaScreenDestination(attachments.toTypedArray(), targetIndex)
                    )
                  },
                  navigateToProfile = { targetAccount ->
                    navigator.navigate(
                      ProfileDestination(targetAccount)
                    )
                  }
                )
                LaunchedEffect(pagerState) {
                  snapshotFlow { pagerState.currentPage }.collect { page ->
                    viewModel.syncExploreKind(page)
                  }
                }
              }
            }
          }
        }
      }
    }
    StatusSnackBar(
      snackbarState = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(
          start = 12.dp,
          end = 12.dp,
          bottom = appState.appPaddingValues.calculateBottomPadding()
        )
    )
  }

  LaunchedEffect(Unit) {
    launch {
      viewModel.snackBarFlow.collect {
        snackbarState.show(it)
      }
    }
    launch {
      appState.scrollToTopFlow.collect {
        when (currentExploreKind) {
          Trending -> trendingStatusListState.scrollToItem(0)
          PublicTimeline -> publicTimelineListState.scrollToItem(0)
          News -> newsListState.scrollToItem(0)
        }
      }
    }
    launch {
      viewModel.searchErrorFlow.collect {
        Toast.makeText(context, "搜索失败", Toast.LENGTH_SHORT).show()
      }
    }
  }

  resultRecipient.onNavResult { result ->
    when (result) {
      is NavResult.Canceled -> Unit
      is NavResult.Value -> viewModel.updateStatusFromDetailScreen(result.value)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTabBar(
  currentExploreKind: ExplorerKind,
  modifier: Modifier = Modifier,
  onTabClick: (Int) -> Unit
) {
  PrimaryTabRow(
    selectedTabIndex = currentExploreKind.ordinal,
    indicator = {
      TabRowDefaults.PrimaryIndicator(
        modifier = Modifier.tabIndicatorOffset(currentExploreKind.ordinal),
        width = 40.dp,
        height = 5.dp,
        color = AppTheme.colors.accent
      )
    },
    divider = { },
    containerColor = Color.Transparent,
    modifier = modifier
  ) {
    ExplorerKind.entries.forEachIndexed { index, kind ->
      val selected = currentExploreKind == kind
      Tab(
        selected = selected,
        onClick = {
          onTabClick(index)
        },
        modifier = Modifier.clip(AppTheme.shape.normal),
        selectedContentColor = Color.Transparent,
        unselectedContentColor = Color.Transparent
      ) {
        Text(
          text = stringResource(kind.stringRes),
          fontSize = 14.sp,
          fontWeight = FontWeight(700),
          color = if (selected) AppTheme.colors.primaryContent else AppTheme.colors.secondaryContent,
          modifier = Modifier.padding(12.dp),
        )
      }
    }
  }
}

@Composable
fun ExploreSearchContent(
  isTextEmpty: Boolean,
  searchingResult: SearchResult?
) {
  Crossfade(targetState = isTextEmpty) {
    when (it) {
      true -> {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Try searching for people, topics or keywords",
            color = AppTheme.colors.primaryContent.copy(0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
      else -> {
        if (searchingResult?.accounts?.isNotEmpty() == true) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            CenterRow {
              Icon(
                painter = painterResource(id = R.drawable.user),
                contentDescription = null,
                tint = AppTheme.colors.primaryContent,
                modifier = Modifier.size(24.dp)
              )
              WidthSpacer(value = 4.dp)
              Text(
                text = "用户",
                color = AppTheme.colors.primaryContent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              WidthSpacer(value = 4.dp)
              Badge(
                containerColor = AppTheme.colors.accent,
                contentColor = Color.White,
                modifier = Modifier.align(Alignment.CenterVertically)
              ) {
                Text(
                  text = when (searchingResult.accounts.size > 10) {
                    true -> "10+"
                    else -> searchingResult.accounts.size.toString()
                  }
                )
              }
            }
            HeightSpacer(value = 8.dp)
            Column(Modifier.padding(start = 8.dp)) {
              searchingResult.accounts.forEach { user ->
                SearchPreviewResultUserItem(user.avatar, user.realDisplayName, user.fullname)
                HeightSpacer(value = 8.dp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SearchPreviewResultUserItem(
  avatar: String,
  username: String,
  instance: String,
) {
  CenterRow {
    CircleShapeAsyncImage(
      model = avatar,
      shape = AppTheme.shape.betweenSmallAndMediumAvatar,
      modifier = Modifier.size(42.dp)
    )
    WidthSpacer(value = 6.dp)
    Column {
      Text(
        text = username,
        color = AppTheme.colors.primaryContent,
        fontSize = 18.sp
      )
      Text(
        text = instance,
        color = AppTheme.colors.primaryContent.copy(0.5f),
        fontSize = 14.sp
      )
    }
  }
}
