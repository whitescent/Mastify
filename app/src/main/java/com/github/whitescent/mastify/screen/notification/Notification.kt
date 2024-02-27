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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.github.whitescent.mastify.network.model.notification.Notification.Type.Mention
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
import com.github.whitescent.mastify.ui.component.WidthSpacer
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

@OptIn(ExperimentalMaterial3Api::class)
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
  val mentionListState = rememberLazyListState()
  val snackbarState = rememberStatusSnackBarState()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()

  val density = LocalDensity.current

  var selectedType by rememberSaveable { mutableStateOf(0) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .systemBarsPadding()
  ) {
    Column {
      NotificationTopBar(activeAccount, Modifier.padding(12.dp)) {
        scope.launch {
          drawerState.open()
        }
      }
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
      ) {
        SegmentedButton(
          selected = selectedType == 0,
          onClick = { selectedType = 0 },
          shape = AppTheme.shape.betweenSmallAndMediumAvatar,
          icon = { },
          border = BorderStroke(0.dp, Color.Transparent),
          colors = SegmentedButtonDefaults.colors(
            activeContainerColor = AppTheme.colors.accent,
            activeContentColor = Color.White,
            inactiveContainerColor = Color(245, 245, 245),
            inactiveContentColor = Color.Gray
          )
        ) {
          Text(text = "Activity")
        }
        WidthSpacer(value = 6.dp)
        SegmentedButton(
          selected = selectedType == 1,
          onClick = { selectedType = 1 },
          icon = { },
          shape = AppTheme.shape.betweenSmallAndMediumAvatar,
          border = BorderStroke(0.dp, Color.Transparent),
          colors = SegmentedButtonDefaults.colors(
            activeContainerColor = AppTheme.colors.accent,
            activeContentColor = Color.White,
            inactiveContainerColor = Color(245, 245, 245),
            inactiveContentColor = Color.Gray
          )
        ) {
          Text(text = "Mentions")
        }
      }
      AnimatedContent(
        targetState = selectedType,
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
      ) { type ->
        when (type) {
          0 -> Activity(
            notifications = notifications.toImmutableList(),
            paginator = viewModel.paginator,
            lazyListState = activityListState,
            navigateToDetail = { navigator.navigate(StatusDetailDestination(it, null)) },
            navigateToProfile = { navigator.navigate(ProfileDestination(it)) },
            acceptRequest = viewModel::acceptFollowRequest,
            rejectRequest = viewModel::rejectFollowRequest
          )
          1 -> Mentions(
            notifications = notifications.filter { it.type is Mention }.toImmutableList(),
            paginator = viewModel.paginator,
            lazyListState = mentionListState,
            navigateToProfile = { navigator.navigate(ProfileDestination(it)) },
            navigateToDetail = { navigator.navigate(StatusDetailDestination(it, null)) }
          )
        }
      }
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
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.show(it)
    }
  }

  appState.scrollToTopFlow.observeWithLifecycle {
    when (selectedType) {
      0 -> activityListState.scrollToItem(0)
      1 -> mentionListState.scrollToItem(0)
    }
  }
}

@Composable
private fun Activity(
  notifications: ImmutableList<NotificationUiData>,
  paginator: Paginator,
  lazyListState: LazyListState,
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
    items(
      items = notifications,
      key = { it.id },
      contentType = { it }
    ) { item ->
      Box {
        Column(
          modifier = Modifier
            .let {
              if (item.unread) it.background(Color(0xFF4685FF).copy(.12f)) else it
            }
            .clickableWithoutIndication {
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
                    navigateToProfile(it)
                  },
                  navigateToDetail = {
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
                  navigateToDetail(item.status!!.actionable)
                },
                navigateToProfile = {
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

@Composable
private fun Mentions(
  notifications: ImmutableList<NotificationUiData>,
  paginator: Paginator,
  lazyListState: LazyListState,
  navigateToProfile: (Account) -> Unit,
  navigateToDetail: (Status) -> Unit
) {
  LazyPagingList(
    paginator = paginator,
    list = notifications.toImmutableList(),
    lazyListState = lazyListState,
    pagePlaceholderType = PagePlaceholderType.Normal,
    contentPadding = PaddingValues(bottom = 100.dp),
    enablePullRefresh = true
  ) {
    items(
      items = notifications,
      key = { it.id },
      contentType = { it }
    ) { item ->
      if (item.status != null) {
        BasicEvent(
          event = item.type as BasicEvent,
          createdAt = item.createdAt,
          actionAccount = item.account,
          status = item.status,
          modifier = Modifier
            .let {
              if (item.unread) it.background(Color(0xFF4685FF).copy(.12f)) else it
            }
            .clickableWithoutIndication {
              navigateToDetail(item.status.actionable)
            }
            .padding(horizontal = 12.dp),
          navigateToProfile = {
            navigateToProfile(it)
          },
          navigateToDetail = {
            navigateToDetail(it)
          }
        )
        AppHorizontalDivider(Modifier.padding(vertical = 14.dp))
      }
    }
  }
}
