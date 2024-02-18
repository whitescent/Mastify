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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LazyPagingList
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.TextWithEmoji
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.status.LazyTimelinePagingList
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.clickableWithoutIndication
import com.github.whitescent.mastify.viewModel.SearchViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SearchResultPager(
  pagerState: PagerState,
  viewModel: SearchViewModel,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToTagInfo: (String) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  val statusListState = rememberLazyListState()
  val accountsListState = rememberLazyListState()
  val hashtagsListState = rememberLazyListState()

  val statusList by viewModel.statusList.collectAsStateWithLifecycle()
  val accountList by viewModel.accounts.collectAsStateWithLifecycle()
  val hashtagList by viewModel.hashtags.collectAsStateWithLifecycle()

  HorizontalPager(
    state = pagerState,
    pageContent = { page ->
      when (page) {
        0 -> LazyTimelinePagingList(
          lazyListState = statusListState,
          paginator = viewModel.statusPaginator,
          pagingList = statusList.toImmutableList(),
          pagePlaceholderType = PagePlaceholderType.Normal,
          action = { action, status ->
            viewModel.onStatusAction(action, status)
          },
          enablePullRefresh = true,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToTagInfo = navigateToTagInfo,
          navigateToMedia = navigateToMedia,
        )
        1 -> {
          LazyPagingList(
            paginator = viewModel.accountsPaginator,
            list = accountList.toImmutableList(),
            lazyListState = accountsListState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            pagePlaceholderType = PagePlaceholderType.Normal
          ) {
            items(accountList) {
              CenterRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickableWithoutIndication {
                    navigateToProfile(it)
                  }
              ) {
                Row(Modifier.weight(1f)) {
                  CircleShapeAsyncImage(
                    model = it.avatar,
                    modifier = Modifier.size(42.dp),
                    shape = AppTheme.shape.betweenSmallAndMediumAvatar
                  ) { navigateToProfile(it) }
                  WidthSpacer(value = 6.dp)
                  Column {
                    Column {
                      TextWithEmoji(
                        text = it.realDisplayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.primaryContent,
                        emojis = it.emojis,
                      )
                      Text(
                        text = it.fullname,
                        fontSize = 14.sp,
                        color = AppTheme.colors.primaryContent.copy(0.45f)
                      )
                    }
                    HeightSpacer(value = 4.dp)
                    HtmlText(
                      text = it.noteWithEmoji,
                      fontSize = 13.8.sp,
                      color = AppTheme.colors.primaryContent,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                      fontWeight = FontWeight(550)
                    )
                  }
                }
              }
            }
          }
        }
        2 -> {
          LazyPagingList(
            paginator = viewModel.hashtagsPaginator,
            list = hashtagList.toImmutableList(),
            lazyListState = hashtagsListState,
            pagePlaceholderType = PagePlaceholderType.Normal,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(hashtagList) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickableWithoutIndication(onClick = { navigateToTagInfo(it.name) })
                  .padding(8.dp),
              ) {
                Text(
                  text = "#${it.name}",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = AppTheme.colors.primaryContent.copy(.85f)
                )
                HeightSpacer(value = 4.dp)
                Text(
                  text = pluralStringResource(id = R.plurals.post_count, it.posts, it.posts),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium,
                  color = AppTheme.colors.primaryContent.copy(.55f),
                )
              }
              AppHorizontalDivider()
            }
          }
        }
      }
    },
    modifier = Modifier.fillMaxSize(),
  )
}
