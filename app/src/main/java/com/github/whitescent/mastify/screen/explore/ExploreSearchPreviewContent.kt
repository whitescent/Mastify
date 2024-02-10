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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.clickableWithoutIndication

@Composable
fun ExploreSearchPreviewContent(
  isTextEmpty: Boolean,
  searchingResult: SearchResult?,
  navigateToAccount: (Account) -> Unit,
  navigateToTag: () -> Unit
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
            text = stringResource(id = R.string.search_description),
            color = AppTheme.colors.primaryContent.copy(0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
          )
        }
      }
      else -> {
        if (searchingResult != null) {
          Column(
            modifier = Modifier
              .padding(horizontal = 12.dp)
              .verticalScroll(rememberScrollState())
          ) {
            SearchPreviewPanel(PreviewType.Account, searchingResult, navigateToAccount, navigateToTag)
            HeightSpacer(value = 8.dp)
            SearchPreviewPanel(PreviewType.Tags, searchingResult, navigateToAccount, navigateToTag)
          }
        }
      }
    }
  }
}

enum class PreviewType {
  Account, Tags
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchPreviewPanel(
  type: PreviewType,
  searchingResult: SearchResult,
  navigateToAccount: (Account) -> Unit,
  navigateToTag: () -> Unit
) {
  when (type) {
    PreviewType.Account -> if (searchingResult.accounts.isEmpty()) return
    PreviewType.Tags -> if (searchingResult.hashtags.isEmpty()) return
  }
  Box(
    modifier = Modifier
      .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
      .border(0.4.dp, AppTheme.colors.searchPreviewBorder, AppTheme.shape.betweenSmallAndMediumAvatar)
      .background(AppTheme.colors.searchPreviewBackground)
  ) {
    Column(Modifier.padding(vertical = 16.dp)) {
      CenterRow(
        modifier = Modifier
          .fillMaxWidth()
          .clickableWithoutIndication {
            when (type) {
              PreviewType.Account -> { }
              PreviewType.Tags -> { }
            }
          }
          .padding(horizontal = 16.dp)
      ) {
        CenterRow(Modifier.weight(1f)) {
          Icon(
            painter = painterResource(
              id = when (type) {
                PreviewType.Account -> R.drawable.user_list
                PreviewType.Tags -> R.drawable.hash_tag
              }
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppTheme.colors.primaryContent
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = when (type) {
              PreviewType.Account ->
                pluralStringResource(R.plurals.search_preview_user, searchingResult.accounts.size, searchingResult.accounts.size)
              PreviewType.Tags ->
                pluralStringResource(R.plurals.search_preview_tags, searchingResult.hashtags.size, searchingResult.hashtags.size)
            },
            color = AppTheme.colors.primaryContent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
          )
        }
        Icon(
          painter = painterResource(R.drawable.right_arrow),
          contentDescription = null,
          modifier = Modifier.size(20.dp),
          tint = AppTheme.colors.primaryContent
        )
      }
      AppHorizontalDivider(Modifier.fillMaxWidth().padding(top = 12.dp))
      when (type) {
        PreviewType.Account -> {
          searchingResult.accounts.forEach {
            val isLatest = it == searchingResult.accounts.last()
            SearchPreviewResultUserItem(
              avatar = it.avatar,
              username = it.realDisplayName,
              instance = it.fullname,
              modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutIndication {
                  navigateToAccount(it)
                }
                .let { modifier ->
                  if (!isLatest) modifier.padding(12.dp) else modifier.padding(12.dp, 12.dp, 12.dp)
                }
            )
            if (!isLatest) AppHorizontalDivider(Modifier.fillMaxWidth())
          }
        }
        PreviewType.Tags -> {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
          ) {
            searchingResult.hashtags.forEach {
              SearchPreviewResultHashtagItem(it.name, navigateToTag)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SearchPreviewResultHashtagItem(name: String, onClick: () -> Unit) {
  Surface(
    shape = AppTheme.shape.betweenSmallAndMediumAvatar,
    color = Color(0xFFF7D86B),
    onClick = onClick
  ) {
    Text(
      text = "#$name",
      color = Color.Black,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    )
  }
}

@Composable
private fun SearchPreviewResultUserItem(
  avatar: String,
  username: String,
  instance: String,
  modifier: Modifier = Modifier
) {
  CenterRow(modifier = modifier) {
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
