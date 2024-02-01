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

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.AnimatedVisibility
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.AvatarWithCover
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.TextWithEmoji
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.avatarStartPadding
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.ProfileLayout
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.rememberProfileLayoutState
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.github.whitescent.mastify.viewModel.ProfileKind
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.launch

data class ProfileNavArgs(val account: Account)

@OptIn(ExperimentalFoundationApi::class)
@AppNavGraph
@Destination(
  navArgsDelegate = ProfileNavArgs::class
)
@Composable
fun Profile(
  appState: AppState,
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
        Column {
          AvatarWithCover(
            cover = {
              if (uiState.account.isEmptyHeader) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(AppTheme.colors.defaultHeader),
                )
              } else {
                AsyncImage(
                  model = uiState.account.header,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxWidth().height(200.dp),
                )
              }
            },
            avatar = {
              CircleShapeAsyncImage(
                model = uiState.account.avatar,
                modifier = Modifier
                  .graphicsLayer {
                    scaleY = (1 - profileLayoutState.progress).coerceAtLeast(0.7f)
                    scaleX = (1 - profileLayoutState.progress).coerceAtLeast(0.7f)
                  }
                  .shadow(12.dp, AppTheme.shape.largeAvatar)
                  .size(80.dp),
                shape = AppTheme.shape.largeAvatar
              )
            },
            actions = {
              CenterRow(
                modifier = Modifier.padding(12.dp)
              ) {
                Text(
                  text = stringResource(
                    id = R.string.account_joined,
                    FormatFactory.getLocalizedDateTime(uiState.account.createdAt)
                  ),
                  color = AppTheme.colors.primaryContent.copy(0.4f),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                )
                WidthSpacer(value = 6.dp)
                Icon(
                  painter = painterResource(R.drawable.shooting_star),
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = AppTheme.colors.primaryContent
                )
              }
            },
          )
          HeightSpacer(value = 10.dp)
          ProfileInfo(uiState.account, uiState.isSelf, uiState.isFollowing)
          HeightSpacer(value = 6.dp)
        }
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
          account = uiState.account,
          topPadding = appState.appPaddingValues.calculateTopPadding(),
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
fun ProfileTopBar(
  alpha: () -> Float,
  account: Account,
  topPadding: Dp,
) {
  val defaultBackgroundColor = AppTheme.colors.defaultHeader
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp + topPadding)
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(account.header)
        .build(),
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
        .alpha(alpha())
        .drawWithContent {
          drawRect(defaultBackgroundColor)
          drawContent()
          drawRect(Color.Black.copy(0.35f))
        },
      contentScale = ContentScale.Crop
    )
    AnimatedVisibility(
      visible = alpha() >= 1,
      enter = slideInVertically { it } + fadeIn(),
      exit = slideOutVertically { it / 2 } + fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      CenterRow(Modifier.statusBarsPadding().padding(start = 24.dp).width(280.dp)) {
        CircleShapeAsyncImage(
          model = account.avatar,
          modifier = Modifier.size(36.dp),
          shape = AppTheme.shape.smallAvatar
        )
        WidthSpacer(value = 8.dp)
        Column {
          TextWithEmoji(
            text = account.realDisplayName,
            emojis = account.emojis,
            fontSize = 18.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = stringResource(id = R.string.post_count, account.statusesCount),
            fontSize = 14.sp,
            color = Color.White,
          )
        }
      }
    }
  }
}

@Composable
fun ProfileInfo(account: Account, isSelf: Boolean?, isFollowing: Boolean?) {
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  Column(Modifier.padding(horizontal = avatarStartPadding)) {
    Row(Modifier.fillMaxWidth()) {
      Column(Modifier.weight(1f)) {
        TextWithEmoji(
          text = account.realDisplayName,
          emojis = account.emojis,
          fontSize = 22.sp,
          fontWeight = FontWeight(650),
          color = AppTheme.colors.primaryContent
        )
        HeightSpacer(value = 2.dp)
        Text(
          text = account.fullname,
          style = AppTheme.typography.statusUsername.copy(
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
          ),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          fontSize = 16.sp,
        )
      }
    }
    if (account.note.isNotEmpty()) {
      HeightSpacer(value = 8.dp)
      HtmlText(
        text = account.noteWithEmoji,
        style = TextStyle(fontSize = 16.sp, color = AppTheme.colors.primaryContent),
        onLinkClick = {
          launchCustomChromeTab(
            context = context,
            uri = Uri.parse(it),
            toolbarColor = primaryColor.toArgb(),
          )
        }
      )
    }
    UserMetricPanel(
      fields = {
        if (account.fieldsWithEmoji.isNotEmpty()) HeightSpacer(value = 8.dp)
        account.fieldsWithEmoji.forEach {
          Row {
            Box(Modifier.width(140.dp), Alignment.CenterStart) {
              CenterRow {
                HtmlText(
                  text = it.name,
                  color = Color(0xFF8B8B8B),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium,
                )
                it.verifiedAt?.let {
                  WidthSpacer(value = 4.dp)
                  Icon(
                    painter = painterResource(id = R.drawable.seal_check),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF00BA7C)
                  )
                }
              }
            }
            WidthSpacer(value = 8.dp)
            HtmlText(
              text = it.value,
              fontSize = 16.sp,
              fontWeight = FontWeight(450),
              overflow = TextOverflow.Ellipsis,
              onLinkClick = { url ->
                launchCustomChromeTab(
                  context = context,
                  uri = Uri.parse(url),
                  toolbarColor = primaryColor.toArgb(),
                )
              },
              modifier = Modifier.weight(1f)
            )
          }
        }
      },
      profileMetrics = {
        val metrics = listOf(account.statusesCount, account.followingCount, account.followersCount)
        metrics.forEachIndexed { index, item ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$item",
              fontWeight = FontWeight.SemiBold,
              fontSize = 20.sp,
              color = AppTheme.colors.primaryContent
            )
            WidthSpacer(value = 4.dp)
            Text(
              text = stringResource(
                id = when (index) {
                  0 -> R.string.post_title
                  1 -> R.string.following_title
                  else -> R.string.follower_title
                }
              ),
              color = AppTheme.colors.primaryContent.copy(0.5f),
              fontSize = 14.sp
            )
          }
        }
      },
      actionButton = {
        isSelf?.let {
          HeightSpacer(value = 10.dp)
          when (it) {
            true -> EditProfileButton(Modifier.fillMaxWidth())
            else -> isFollowing?.let { FollowButton(isFollowing) }
          }
          HeightSpacer(value = 4.dp)
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTabs(
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

@Composable
fun UserMetricPanel(
  fields: @Composable (ColumnScope.() -> Unit)? = null,
  profileMetrics: @Composable (RowScope.() -> Unit)? = null,
  actionButton: @Composable () -> Unit
) {
  Column(Modifier.fillMaxWidth()) {
    fields?.let {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        fields.invoke(this)
      }
      HeightSpacer(value = 10.dp)
    }
    CenterRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(36.dp)
    ) {
      profileMetrics?.invoke(this)
    }
    actionButton()
  }
}

@Composable
fun FollowButton(isFollowing: Boolean) {
  Box(
    modifier = Modifier.fillMaxWidth()
      .border(
        width = 2.dp,
        color = if (isFollowing) AppTheme.colors.unfollowButton else AppTheme.colors.followButton,
        shape = AppTheme.shape.mediumAvatar
      )
      .clip(AppTheme.shape.mediumAvatar),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(
        id = if (isFollowing) R.string.unfollow_title else R.string.follow_title
      ),
      color = AppTheme.colors.primaryContent,
      modifier = Modifier.padding(10.dp),
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun EditProfileButton(modifier: Modifier = Modifier) {
  Surface(
    shape = AppTheme.shape.normal,
    color = AppTheme.colors.secondaryContent,
    modifier = modifier
  ) {
    CenterRow(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        painter = painterResource(id = R.drawable.pencil_simple_line),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = Color.White
      )
      WidthSpacer(value = 6.dp)
      Text(
        text = stringResource(id = R.string.edit_profile),
        fontSize = 16.sp,
        color = Color.White,
      )
    }
  }
}
