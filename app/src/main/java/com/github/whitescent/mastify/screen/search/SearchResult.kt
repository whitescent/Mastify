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

package com.github.whitescent.mastify.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.SearchResultDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.screen.destinations.TagInfoDestination
import com.github.whitescent.mastify.screen.explore.ExploreSearchBar
import com.github.whitescent.mastify.screen.explore.SearchNavigateType
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.SearchType
import com.github.whitescent.mastify.viewModel.SearchViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.launch

data class SearchResultNavArgs(
  val searchQuery: String,
  val searchType: SearchNavigateType?
)

@Destination(
  navArgsDelegate = SearchResultNavArgs::class
)
@Composable
fun SearchResult(
  navigator: DestinationsNavigator,
  viewModel: SearchViewModel = hiltViewModel(),
  resultRecipient: ResultRecipient<StatusDetailDestination, StatusBackResult>
) {
  val uiState = viewModel.uiState

  val currentSearchType by viewModel.currentSearchType.collectAsStateWithLifecycle()

  val scope = rememberCoroutineScope()
  val pagerState = rememberPagerState(
    initialPage = when (viewModel.navArgs.searchType) {
      null -> 0
      SearchNavigateType.Account -> 1
      SearchNavigateType.Tags -> 2
    }
  ) { SearchType.entries.size }

  val focusRequester = remember { FocusRequester() }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .systemBarsPadding()
  ) {
    Column(Modifier.fillMaxWidth()) {
      CenterRow(Modifier.padding(horizontal = 10.dp)) {
        IconButton(
          onClick = { navigator.popBackStack() },
          modifier = Modifier.padding(end = 8.dp).size(28.dp),
          colors = IconButtonDefaults.iconButtonColors(
            contentColor = AppTheme.colors.primaryContent
          )
        ) {
          Icon(
            painter = painterResource(id = R.drawable.arrow_left),
            contentDescription = null
          )
        }
        ExploreSearchBar(
          text = uiState.query,
          focusRequester = focusRequester,
          onValueChange = viewModel::updateQuery,
          clearText = viewModel::clearQuery,
          onFocusChange = { },
        ) {
          navigator.navigate(SearchResultDestination(SearchResultNavArgs(uiState.query, null)))
        }
      }
      HeightSpacer(value = 4.dp)
      SearchResultTabBar(
        currentSearchType = currentSearchType,
        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
      ) { kind ->
        viewModel.updateSearchType(kind)
        scope.launch {
          pagerState.scrollToPage(kind)
        }
      }
      AppHorizontalDivider(thickness = 1.dp)
      SearchResultPager(
        pagerState = pagerState,
        viewModel = viewModel,
        navigateToDetail = {
          navigator.navigate(StatusDetailDestination(it.actionableStatus, null))
        },
        navigateToTagInfo = {
          navigator.navigate(TagInfoDestination(it))
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
    }
  }

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.currentPage }.collect { page ->
      viewModel.updateSearchType(page)
    }
  }

  resultRecipient.onNavResult { result ->
    when (result) {
      is NavResult.Canceled -> Unit
      is NavResult.Value -> viewModel.updateStatusFromDetailScreen(result.value)
    }
  }
}


@Composable
fun SearchResultTabBar(
  currentSearchType: SearchType,
  modifier: Modifier = Modifier,
  onTabClick: (Int) -> Unit
) {
  PrimaryTabRow(
    selectedTabIndex = currentSearchType.ordinal,
    indicator = {
      TabRowDefaults.PrimaryIndicator(
        modifier = Modifier.tabIndicatorOffset(currentSearchType.ordinal),
        width = 40.dp,
        height = 5.dp,
        color = AppTheme.colors.accent
      )
    },
    divider = { },
    containerColor = Color.Transparent,
    modifier = modifier
  ) {
    SearchType.entries.forEachIndexed { index, kind ->
      val selected = currentSearchType == kind
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
          text = stringResource(kind.label),
          fontSize = 14.sp,
          fontWeight = FontWeight(700),
          color = if (selected) AppTheme.colors.primaryContent else AppTheme.colors.secondaryContent,
          modifier = Modifier.padding(12.dp),
        )
      }
    }
  }
}
