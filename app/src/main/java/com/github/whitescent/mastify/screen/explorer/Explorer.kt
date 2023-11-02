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

package com.github.whitescent.mastify.screen.explorer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@AppNavGraph
@Destination(
  style = BottomBarScreenTransitions::class
)
@Composable
fun Explorer(
  viewModel: ExplorerViewModel = hiltViewModel(),
  appState: AppState,
  navigator: DestinationsNavigator
) {
  val uiState = viewModel.uiState
  val context = LocalContext.current
  val tabs = listOf("热门嘟文", "新闻", "公共时间轴")
  var selectedTab by remember { mutableIntStateOf(0) }

  val pagerState = rememberPagerState { tabs.size }
  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()

  val trendingStatusList = viewModel.trendingStatusPager.collectAsLazyPagingItems()

  val trendingStatusListState = rememberLazyListState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .background(AppTheme.colors.background)
  ) {
    Column {
      Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)) {
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
        ExplorerSearchBar(
          text = uiState.text,
          onValueChange = viewModel::onValueChange
        )
        HeightSpacer(value = 4.dp)
      }
      Column {
        ExplorerTabBar(
          tabs = listOf("热门嘟文", "新闻", "公共时间轴"),
          selectedTab = selectedTab,
          modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
        ) {
          selectedTab = it
          scope.launch {
            pagerState.scrollToPage(it)
          }
        }
        AppHorizontalDivider(thickness = 1.dp)
        ExplorerPager(
          state = pagerState,
          trendingStatusListState = trendingStatusListState,
          trendingStatusList = trendingStatusList,
          action = {
            viewModel.onStatusAction(it, context)
          },
          navigateToDetail = {
            navigator.navigate(
              StatusDetailDestination(
                avatar = uiState.avatar,
                status = it
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
          }
        )
        LaunchedEffect(pagerState) {
          snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedTab = page
          }
        }
      }
    }
    StatusSnackBar(
      snackbarState = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 12.dp, end = 12.dp, bottom = appState.appPaddingValues.calculateBottomPadding())
    )
  }

  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.show(it)
    }
  }
}

@Composable
fun ExplorerSearchBar(
  text: String,
  onValueChange: (String) -> Unit
) {
  BasicTextField(
    value = text,
    onValueChange = onValueChange,
    textStyle = TextStyle(fontSize = 16.sp),
    singleLine = true
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, Color(0xFF5A9BFA).copy(alpha = 0.46f), RoundedCornerShape(12.dp))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp)
          .border(2.dp, Color(0xFF5A9BFA), RoundedCornerShape(10.dp))
      ) {
        CenterRow(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
          Icon(
            painter = painterResource(id = R.drawable.search),
            contentDescription = null,
            tint = AppTheme.colors.cardAction,
            modifier = Modifier.size(24.dp)
          )
          WidthSpacer(value = 6.dp)
          it()
        }
      }
    }
  }
}

@Composable
fun ExplorerTabBar(
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
