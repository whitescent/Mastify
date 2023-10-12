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

package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.mapper.status.getReplyChainType
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.StatusAction
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.statusComment(
  descendants: ImmutableList<StatusUiData>,
  action: (StatusAction) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (List<Status.Attachment>, Int) -> Unit,
) {
  when (descendants.isEmpty()) {
    true -> item {
      StatusEndIndicator(Modifier.padding(36.dp))
    }
    else -> {
      itemsIndexed(
        items = descendants,
        key = { _, item -> item.id }
      ) { index, item ->
        val replyChainType = remember(item) { descendants.getReplyChainType(index) }
        StatusListItem(
          status = item,
          action = action,
          replyChainType = replyChainType,
          hasUnloadedParent = false,
          navigateToDetail = { navigateToDetail(item.actionable) },
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile,
          modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp)
            .animateItemPlacement(),
        )
        if (replyChainType == Null || replyChainType == End) AppHorizontalDivider()
      }
      item {
        StatusEndIndicator(Modifier.padding(36.dp))
      }
    }
  }
}

fun LazyListScope.statusLoadingIndicator() {
  item {
    Box(Modifier.fillMaxWidth().padding(bottom = 56.dp), Alignment.Center) {
      Column {
        HeightSpacer(value = 8.dp)
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = AppTheme.colors.primaryContent,
          strokeWidth = 2.dp
        )
      }
    }
  }
}

@Composable
fun StatusEndIndicator(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    Box(Modifier.size(4.dp).background(Color.Gray, CircleShape))
  }
}
