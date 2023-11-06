/*
 * Copyright 2023 WhiteScent
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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Down
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Up
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
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
import com.github.whitescent.mastify.viewModel.ExplorerViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@AppNavGraph
@Destination(
  style = BottomBarScreenTransitions::class
)
@Composable
fun Explore(
  viewModel: ExplorerViewModel = hiltViewModel(),
  appState: AppState,
  navigator: DestinationsNavigator
) {
  val uiState = viewModel.uiState
  val context = LocalContext.current
  val tabs = listOf("热门嘟文", "新闻", "公共时间轴")
  var selectedTab by remember { mutableIntStateOf(0) }
  var hideContent by remember { mutableStateOf(false) }
  // when user focus on searchBar, we need hide content

  val pagerState = rememberPagerState { tabs.size }
  val focusRequester = remember { FocusRequester() }

  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()
  val trendingStatusListState = rememberLazyListState()
  val trendingStatusList = viewModel.trendingStatusPager.collectAsLazyPagingItems()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .background(AppTheme.colors.background)
  ) {
    Column {
      Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)) {
        AnimatedContent(
          targetState = hideContent,
          transitionSpec = {
            slideIntoContainer(Down) togetherWith slideOutOfContainer(Up)
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          if (!it) {
            Column {
              CenterRow {
                Text(
                  text = "探索 m.cmx.im",
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = AppTheme.colors.primaryContent,
                  modifier = Modifier.weight(1f)
                )
                CircleShapeAsyncImage(
                  model = uiState.avatar,
                  modifier = Modifier
                    .size(36.dp)
                    .shadow(12.dp, AppTheme.shape.betweenSmallAndMediumAvatar),
                  shape = AppTheme.shape.betweenSmallAndMediumAvatar
                )
              }
              HeightSpacer(value = 6.dp)
            }
          }
        }
        ExploreSearchBar(
          text = uiState.text,
          focusRequester = focusRequester,
          onValueChange = viewModel::onValueChange,
          onFocusChange = { hideContent = it }
        )
        HeightSpacer(value = 4.dp)
      }
      Crossfade(
        targetState = hideContent,
        animationSpec = tween()
      ) {
        when (it) {
          true -> {
            ExploreSearchContent(isTextEmpty = uiState.text.isEmpty())
          }
          else -> {
            Column {
              ExploreTabBar(
                tabs = listOf("热门嘟文", "新闻", "公共时间轴"),
                selectedTab = selectedTab,
                modifier = Modifier
                  .padding(horizontal = 12.dp)
                  .fillMaxWidth()
              ) { currentTab ->
                selectedTab = currentTab
                scope.launch {
                  pagerState.scrollToPage(currentTab)
                }
              }
              AppHorizontalDivider(thickness = 1.dp)
              ExplorePager(
                state = pagerState,
                trendingStatusListState = trendingStatusListState,
                trendingStatusList = trendingStatusList,
                action = { action ->
                  viewModel.onStatusAction(action, context)
                },
                navigateToDetail = { targetStatus ->
                  navigator.navigate(
                    StatusDetailDestination(
                      avatar = uiState.avatar,
                      status = targetStatus
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
                  selectedTab = page
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
        trendingStatusListState.scrollToItem(0)
      }
    }
  }
}

@Composable
fun ExploreSearchBar(
  text: String,
  focusRequester: FocusRequester,
  onValueChange: (String) -> Unit,
  onFocusChange: (Boolean) -> Unit
) {
  BasicTextField(
    value = text,
    onValueChange = onValueChange,
    textStyle = TextStyle(fontSize = 16.sp),
    singleLine = true,
    cursorBrush = SolidColor(AppTheme.colors.primaryContent),
    modifier = Modifier
      .focusable()
      .focusRequester(focusRequester)
      .onFocusChanged {
        onFocusChange(it.isFocused)
      }
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      shadowElevation = 1.dp,
      shape = AppTheme.shape.betweenSmallAndMediumAvatar,
      color = AppTheme.colors.exploreSearchBarBackground,
      border = BorderStroke(1.dp, AppTheme.colors.exploreSearchBarBorder),
    ) {
      CenterRow(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
        Icon(
          painter = painterResource(id = R.drawable.search),
          contentDescription = null,
          tint = AppTheme.colors.primaryContent,
          modifier = Modifier.size(24.dp)
        )
        WidthSpacer(value = 6.dp)
        Box(contentAlignment = Alignment.CenterStart) {
          if (text.isEmpty()) {
            Text(
              text = "搜索", // TODO Localization
              color = AppTheme.colors.primaryContent.copy(0.5f),
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
          it()
        }
      }
    }
  }
}

@Composable
fun ExploreTabBar(
  tabs: List<String>,
  selectedTab: Int,
  modifier: Modifier = Modifier,
  onTabClick: (Int) -> Unit
) {
  PrimaryTabRow(
    selectedTabIndex = selectedTab,
    indicator = {
      TabRowDefaults.PrimaryIndicator(
        modifier = Modifier.tabIndicatorOffset(it[selectedTab]),
        width = 40.dp,
        height = 5.dp,
        color = AppTheme.colors.accent
      )
    },
    divider = { },
    containerColor = Color.Transparent,
    modifier = modifier
  ) {
    tabs.forEachIndexed { index, tab ->
      val selected = selectedTab == index
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
          text = tab,
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
  isTextEmpty: Boolean
) {
  Crossfade(targetState = isTextEmpty) {
    when (it) {
      true -> {
        Column {
        }
      }
      else -> {
      }
    }
  }
}
