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

package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.screen.destinations.TagInfoDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.ProfileLayout
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.rememberProfileLayoutState
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.ProfileKind
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.launch

data class ProfileNavArgs(val account: Account)

@AppNavGraph
@Destination(
  navArgsDelegate = ProfileNavArgs::class
)
@Composable
fun Profile(
  navigator: DestinationsNavigator,
  viewModel: ProfileViewModel = hiltViewModel(),
  resultRecipient: ResultRecipient<StatusDetailDestination, StatusBackResult>
) {
  val uiState = viewModel.uiState
  val currentTab by viewModel.currentProfileKind.collectAsStateWithLifecycle()

  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()
  val profileLayoutState = rememberProfileLayoutState()
  val statusListState = rememberLazyListState()
  val statusWithReplyListState = rememberLazyListState()
  val statusWithMediaListState = rememberLazyListState()

  val atPageTop by remember {
    derivedStateOf {
      profileLayoutState.progress == 0f
    }
  }

  Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
    ProfileLayout(
      state = profileLayoutState,
      collapsingTop = {
        ProfileHeader(
          uiState = uiState,
          profileLayoutState = profileLayoutState,
          follow = viewModel::followAccount,
          subscribe = { viewModel.followAccount(true, it) },
          searchAccount = viewModel::lookupAccount,
          navigateToAccount = {
            navigator.navigate(ProfileDestination(uiState.searchedAccount!!))
          },
          navigateToTagInfo = {
            navigator.navigate(TagInfoDestination(it))
          }
        )
      },
      bodyContent = {
        val pagerState = rememberPagerState { ProfileKind.entries.size }
        Column {
          ProfileTabs(currentTab) {
            if (currentTab.ordinal == it) {
              scope.launch {
                when (it) {
                  0 -> statusListState.scrollToItem(0)
                  1 -> statusWithReplyListState.scrollToItem(0)
                  else -> statusWithMediaListState.scrollToItem(0)
                }
              }.invokeOnCompletion { profileLayoutState.animatedToTop() }
            }
            viewModel.syncProfileTab(it)
            scope.launch {
              pagerState.scrollToPage(it)
            }
          }
          if (uiState.isSelf != null) {
            ProfilePager(
              state = pagerState,
              viewModel = viewModel,
              statusListState = statusListState,
              statusListWithReplyState = statusWithReplyListState,
              statusListWithMediaState = statusWithMediaListState,
              navigateToDetail = {
                navigator.navigate(
                  StatusDetailDestination(
                    status = it,
                    originStatusId = null
                  )
                )
              },
              navigateToTagInfo = {
                navigator.navigate(TagInfoDestination(it))
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
            )
          }
        }
        LaunchedEffect(pagerState) {
          snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.syncProfileTab(page)
          }
        }
      },
      topBar = {
        ProfileTopBar(
          alpha = { profileLayoutState.progress },
          account = uiState.account
        )
      },
    )
    StatusSnackBar(
      snackbarState = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 12.dp, end = 12.dp, bottom = 36.dp)
    )
  }

  LaunchedEffect(atPageTop) {
    if (atPageTop) {
      scope.launch {
        statusListState.scrollToItem(0)
        statusWithReplyListState.scrollToItem(0)
        statusWithMediaListState.scrollToItem(0)
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.show(it)
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
private fun ProfileTabs(
  selectedTab: ProfileKind,
  onTabClick: (Int) -> Unit
) {
  PrimaryTabRow(
    selectedTabIndex = selectedTab.ordinal,
    indicator = {
      TabRowDefaults.PrimaryIndicator(
        modifier = Modifier.tabIndicatorOffset(selectedTab.ordinal),
        width = 40.dp,
        height = 5.dp,
        color = AppTheme.colors.accent
      )
    },
    containerColor = Color.Transparent,
    divider = {
      AppHorizontalDivider(thickness = 1.dp)
    },
  ) {
    ProfileKind.entries.forEachIndexed { index, tab ->
      val selected = selectedTab == tab
      Tab(
        selected = selected,
        onClick = {
          onTabClick(index)
        },
        modifier = Modifier.clip(AppTheme.shape.normal)
      ) {
        Text(
          text = stringResource(tab.stringRes),
          fontSize = 17.sp,
          fontWeight = FontWeight(700),
          color = if (selected) AppTheme.colors.primaryContent else AppTheme.colors.secondaryContent,
          modifier = Modifier.padding(12.dp),
        )
      }
    }
  }
}
