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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.network.model.notification.Notification
import com.github.whitescent.mastify.paging.LazyPagingList
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.notification.event.BasicEvent
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.BottomBarScreenTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.viewModel.NotificationViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val notificationListState = rememberLazyListState()
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
      HeightSpacer(value = 6.dp)
      LazyPagingList(
        paginator = viewModel.paginator,
        list = notifications.toImmutableList(),
        lazyListState = notificationListState,
        pagePlaceholderType = PagePlaceholderType.Normal,
        contentPadding = PaddingValues(bottom = 100.dp),
        enablePullRefresh = true
      ) {
        itemsIndexed(
          items = notifications,
          key = { _, notification -> notification.id },
          contentType = { _, _ -> Notification },
        ) { index, item ->
          when (item.type) {
            is Notification.Type.BasicEvent -> {
              BasicEvent(
                event = item.type,
                actionAccount = item.account,
                status = item.status!!,
                modifier = Modifier.padding(horizontal = 12.dp),
                navigateToProfile = {
                  navigator.navigate(ProfileDestination(it))
                },
                navigateToDetail = {
                  navigator.navigate(StatusDetailDestination(it, null))
                }
              )
              AppHorizontalDivider(Modifier.padding(vertical = 14.dp))
            }
            else -> Unit
          }
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    appState.scrollToTopFlow.collect {
      notificationListState.animateScrollToItem(0)
    }
  }
}
