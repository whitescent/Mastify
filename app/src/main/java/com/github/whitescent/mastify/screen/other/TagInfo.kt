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

package com.github.whitescent.mastify.screen.other

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.screen.destinations.TagInfoDestination
import com.github.whitescent.mastify.ui.component.AnimatedVisibility
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.button.FollowButton
import com.github.whitescent.mastify.ui.component.status.LazyTimelinePagingList
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.rememberTimelineNestedScrollConnectionState
import com.github.whitescent.mastify.viewModel.TagViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TagInfoNavArgs(val name: String)

@Destination(
  navArgsDelegate = TagInfoNavArgs::class
)
@Composable
fun TagInfo(
  navigator: DestinationsNavigator,
  viewModel: TagViewModel = hiltViewModel(),
  resultRecipient: ResultRecipient<StatusDetailDestination, StatusBackResult>
) {
  val uiState = viewModel.uiState
  val timeline by viewModel.tagTimeline.collectAsStateWithLifecycle()

  var headerHeight by remember { mutableStateOf(0f) }

  val scrollState = rememberTimelineNestedScrollConnectionState(
    scrollThreshold = headerHeight
  )
  val lazyListState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  val showScrollToTopButton by remember {
    derivedStateOf {
      lazyListState.firstVisibleItemIndex > 0
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .let {
        if (timeline.isNotEmpty()) {
          it.scrollable(
            state = rememberScrollableState { offset ->
              scrollState.calculateOffset(offset).y
            },
            orientation = Orientation.Vertical
          ).nestedScroll(scrollState.nestedScrollConnection)
        } else it
      }
  ) {
    LazyTimelinePagingList(
      paginator = viewModel.tagPaginator,
      pagingList = timeline.toImmutableList(),
      lazyListState = lazyListState,
      pagePlaceholderType = PagePlaceholderType.Normal,
      action = viewModel::onStatusAction,
      navigateToDetail = {
        navigator.navigate(StatusDetailDestination(it.actionableStatus, null))
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
      },
      navigateToTagInfo = {
        navigator.navigate(TagInfoDestination(it))
      },
      modifier = Modifier
        .offset {
          IntOffset(
            x = 0,
            y = (scrollState.offset.value + headerHeight).roundToInt()
          )
        }
    )
    Box(
      modifier = Modifier
        .onSizeChanged {
          headerHeight = it.height.toFloat()
        }
        .offset {
          IntOffset(x = 0, y = (scrollState.offset.value).roundToInt())
        }
        .padding(horizontal = 12.dp)
        .statusBarsPadding()
    ) {
      Column {
        CenterRow {
          IconButton(
            onClick = { navigator.popBackStack() },
          ) {
            Icon(
              painter = painterResource(id = R.drawable.arrow_left),
              contentDescription = null,
              tint = AppTheme.colors.primaryContent
            )
          }
        }
        Column {
          HeightSpacer(value = 4.dp)
          CenterRow {
            Box(
              modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF081B34))
            ) {
              Icon(
                painter = painterResource(id = R.drawable.hash_tag),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(10.dp).size(28.dp)
              )
            }
            WidthSpacer(value = 6.dp)
            Column {
              Text(
                text = uiState.hashtag,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.primaryContent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              CenterRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                  text = pluralStringResource(R.plurals.posts_week, uiState.posts, uiState.posts),
                  fontSize = 14.sp,
                  color = AppTheme.colors.primaryContent.copy(.65f),
                )
                Text(
                  text = pluralStringResource(
                    R.plurals.participant_count,
                    uiState.participants,
                    uiState.participants,
                  ),
                  fontSize = 14.sp,
                  color = AppTheme.colors.primaryContent.copy(.65f),
                )
                Text(
                  text = pluralStringResource(
                    R.plurals.posts_today,
                    uiState.postsToday,
                    uiState.postsToday,
                  ),
                  fontSize = 14.sp,
                  color = AppTheme.colors.primaryContent.copy(.65f),
                )
              }
            }
          }
          if (uiState.following != null) {
            FollowButton(
              followed = uiState.following,
              postState = uiState.followState,
              onClick = { viewModel.followHashtag() },
              modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            ) {
              Text(
                text = stringResource(
                  id = when (uiState.following) {
                    true -> R.string.following_hashtag
                    else -> R.string.follow_hashtag
                  },
                  uiState.hashtag
                ),
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
              )
            }
          }
        }
      }
    }
    AnimatedVisibility(
      visible = showScrollToTopButton,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(bottom = 16.dp),
      enter = slideInVertically { it * 2 },
      exit = slideOutVertically { it * 2 }
    ) {
      Button(
        onClick = {
          scope.launch {
            lazyListState.scrollToItem(0)
            scrollState.offset.animateTo(0f)
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = AppTheme.colors.accent
        ),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(bottom = 16.dp)
          .shadow(2.dp, CircleShape)
      ) {
        CenterRow {
          Icon(
            painter = painterResource(id = R.drawable.arrow_up),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          WidthSpacer(value = 2.dp)
          Text(
            text = stringResource(id = R.string.back_to_top),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }

  resultRecipient.onNavResult { result ->
    when (result) {
      is NavResult.Canceled -> Unit
      is NavResult.Value -> viewModel.updateStatusFromDetailScreen(result.value)
    }
  }
}
