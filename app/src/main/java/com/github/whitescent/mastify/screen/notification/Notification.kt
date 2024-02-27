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

package com.github.whitescent.mastify.screen.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.ui.NotificationUiData
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.extensions.observeWithLifecycle
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.notification.Notification.Type.BasicEvent
import com.github.whitescent.mastify.network.model.notification.Notification.Type.SpecialEvent
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LazyPagingList
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.UnreadEvent
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.notification.event.BasicEvent
import com.github.whitescent.mastify.screen.notification.event.FollowEvent
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.BottomBarScreenTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.clickableWithoutIndication
import com.github.whitescent.mastify.viewModel.NotificationViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Destination(style = BottomBarScreenTransitions::class)
@AppNavGraph
@Composable
fun Notification(
  activeAccount: AccountEntity,
  appState: AppState,
  drawerState: DrawerState,
  navigator: DestinationsNavigator,
  viewModel: NotificationViewModel = hiltViewModel()
) {
  val scope = rememberCoroutineScope()
  val activityListState = rememberLazyListState()
  val snackbarState = rememberStatusSnackBarState()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()

  val density = LocalDensity.current

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .systemBarsPadding()
  ) {
    Column {
      NotificationTopBar(
        activeAccount = activeAccount,
        modifier = Modifier.padding(12.dp),
        dismissAllNotification = viewModel::dismissAllNotification
      ) {
        scope.launch {
          drawerState.open()
        }
      }
      NotificationList(
        notifications = notifications.toImmutableList(),
        paginator = viewModel.paginator,
        lazyListState = activityListState,
        navigateToDetail = { navigator.navigate(StatusDetailDestination(it, null)) },
        navigateToProfile = { navigator.navigate(ProfileDestination(it)) },
        dismissNotification = viewModel::dismissNotification,
        acceptRequest = viewModel::acceptFollowRequest,
        rejectRequest = viewModel::rejectFollowRequest
      )
    }
    StatusSnackBar(
      snackbarState = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = WindowInsets.navigationBars.getBottom(density).dp)
        .padding(2.dp)
    )
  }

  LaunchedEffect(Unit) {
    viewModel.unreadFlow.collect {
      when (it) {
        is UnreadEvent.Refresh -> appState.unreadNotifications = it.count
        is UnreadEvent.Append -> appState.unreadNotifications += it.count
        is UnreadEvent.Dismiss -> {
          if (appState.unreadNotifications > 0) appState.unreadNotifications -= 1
        }
        is UnreadEvent.DismissAll -> appState.unreadNotifications = 0
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.show(it)
    }
  }

  appState.scrollToTopFlow.observeWithLifecycle {
    activityListState.scrollToItem(0)
  }
}

@Composable
private fun NotificationList(
  notifications: ImmutableList<NotificationUiData>,
  paginator: Paginator,
  lazyListState: LazyListState,
  dismissNotification: (Int) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  acceptRequest: (String) -> Unit,
  rejectRequest: (String) -> Unit,
) {
  LazyPagingList(
    paginator = paginator,
    list = notifications.toImmutableList(),
    lazyListState = lazyListState,
    pagePlaceholderType = PagePlaceholderType.Normal,
    contentPadding = PaddingValues(bottom = 100.dp),
    enablePullRefresh = true
  ) {
    itemsIndexed(
      items = notifications,
      key = { _, notification -> notification.id },
      contentType = { _, notification -> notification },
    ) { index, item ->
      Box {
        Column(
          modifier = Modifier
            .let {
              if (item.unread) it.background(Color(0xFF4685FF).copy(.12f)) else it
            }
            .clickableWithoutIndication {
              if (item.unread) dismissNotification(index)
              navigateToProfile(item.account)
            }
        ) {
          when (item.type) {
            is BasicEvent -> {
              if (item.status != null) {
                BasicEvent(
                  event = item.type,
                  createdAt = item.createdAt,
                  actionAccount = item.account,
                  status = item.status,
                  navigateToProfile = {
                    if (item.unread) dismissNotification(index)
                    navigateToProfile(it)
                  },
                  navigateToDetail = {
                    if (item.unread) dismissNotification(index)
                    navigateToDetail(it)
                  },
                  modifier = Modifier.padding(12.dp)
                )
              }
            }
            is SpecialEvent -> {
              FollowEvent(
                event = item.type,
                actionAccount = item.account,
                modifier = Modifier.padding(12.dp),
                navigateToDetail = {
                  if (item.unread) dismissNotification(index)
                  navigateToDetail(item.status!!.actionable)
                },
                navigateToProfile = {
                  if (item.unread) dismissNotification(index)
                  navigateToProfile(it)
                },
                acceptRequest = acceptRequest,
                rejectRequest = rejectRequest
              )
            }
            else -> Unit
          }
          AppHorizontalDivider()
        }
      }
    }
  }
}
