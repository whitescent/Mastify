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

import android.provider.SyncStateContract.Helpers.insert
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.screen.destinations.TagInfoDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.EmojiSheet
import com.github.whitescent.mastify.ui.component.ReplyTextField
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.dialog.InReplyToMultiSelectorDialog
import com.github.whitescent.mastify.ui.component.dialog.rememberDialogState
import com.github.whitescent.mastify.ui.component.status.StatusDetailCard
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.component.statusComment
import com.github.whitescent.mastify.ui.component.statusLoadingIndicator
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.viewModel.StatusDetailViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

data class StatusDetailNavArgs(
  val status: Status,
  val originStatusId: String?
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@AppNavGraph
@Destination(
  navArgsDelegate = StatusDetailNavArgs::class
)
@Composable
fun StatusDetail(
  resultNavigator: ResultBackNavigator<StatusBackResult>,
  navigator: DestinationsNavigator,
  resultRecipient: ResultRecipient<StatusDetailDestination, StatusBackResult>,
  viewModel: StatusDetailViewModel = hiltViewModel()
) {
  var openEmojiSheet by remember { mutableStateOf(false) }

  val lazyState = rememberLazyListState()
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()
  val dialogState = rememberDialogState()

  val keyboard = LocalSoftwareKeyboardController.current

  val state = viewModel.uiState

  val currentStatus by viewModel.currentStatus.collectAsStateWithLifecycle()

  Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
    Column {
      Spacer(Modifier.statusBarsPadding())
      StatusDetailTopBar(
        navigationIcon = {
          ClickableIcon(
            painter = painterResource(id = R.drawable.arrow_left),
            onClick = {
              resultNavigator.navigateBack(
                result = StatusBackResult(
                  id = currentStatus.actionableId,
                  favorited = currentStatus.favorited,
                  favouritesCount = currentStatus.favouritesCount,
                  reblogged = currentStatus.reblogged,
                  reblogsCount = currentStatus.reblogsCount,
                  repliesCount = currentStatus.repliesCount,
                  bookmarked = currentStatus.bookmarked,
                  poll = currentStatus.poll
                )
              )
            },
            interactiveSize = 28.dp,
            tint = AppTheme.colors.primaryContent
          )
        },
        title = {
          Text(
            text = stringResource(id = R.string.home_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.primaryContent,
          )
        },
        modifier = Modifier.padding(12.dp)
      )
      AppHorizontalDivider(Modifier.padding(vertical = 6.dp))
      StatusDetailContent(
        currentStatusId = viewModel.navArgs.status.id,
        lazyState = lazyState,
        statusList = state.statusList,
        loading = state.loading,
        action = { action, id ->
          viewModel.onStatusAction(action, id)
        },
        navigateToDetail = {
          if (it.id != viewModel.navArgs.status.id) {
            navigator.navigate(
              StatusDetailDestination(
                status = it,
                originStatusId = null
              )
            )
          }
        },
        navigateToMedia = { attachments, index ->
          navigator.navigate(
            StatusMediaScreenDestination(
              attachments = attachments.toTypedArray(),
              targetMediaIndex = index
            )
          )
        },
        navigateToProfile = {
          navigator.navigate(ProfileDestination(it))
        },
        navigateToTagInfo = {
          navigator.navigate(TagInfoDestination(it))
        },
        modifier = Modifier.weight(1f)
      )
      ReplyTextField(
        targetAccount = when (currentStatus.isInReplyTo) {
          true -> state.threadList.filter { it.selected }.map { it.account }
          else -> listOf(viewModel.navArgs.status.account)
        },
        textFieldState = viewModel.replyField,
        postState = state.postState,
        replyToStatus = viewModel::replyToStatus,
        openEmojiPicker = { openEmojiSheet = true },
        showReplyUserButton = state.threadList.size > 1,
        openReplyUserDialog = { dialogState.showDialog() }
      )
    }
    StatusSnackBar(
      snackbarState = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 12.dp, end = 12.dp, bottom = 56.dp)
    )
  }

  InReplyToMultiSelectorDialog(
    dialogState = dialogState,
    threads = state.threadList,
    onClick = viewModel::updateThreads
  )

  if (openEmojiSheet) {
    EmojiSheet(
      sheetState = sheetState,
      emojis = state.instanceEmojis,
      onSelectEmoji = {
        viewModel.replyField.edit {
          insert(selectionInChars.start, it)
        }
        scope.launch {
          sheetState.hide()
        }.invokeOnCompletion {
          keyboard?.show()
          openEmojiSheet = false
        }
      },
      onDismissRequest = {
        openEmojiSheet = false
      }
    )
  }

  resultRecipient.onNavResult { result ->
    when (result) {
      is NavResult.Canceled -> Unit
      is NavResult.Value -> viewModel.updateStatusFromDetailScreen(result.value)
    }
  }

  BackHandler {
    // we need sync the latest status action data to previous screen
    resultNavigator.navigateBack(
      result = StatusBackResult(
        id = viewModel.navArgs.status.id,
        favorited = currentStatus.favorited,
        favouritesCount = currentStatus.favouritesCount,
        reblogged = currentStatus.reblogged,
        reblogsCount = currentStatus.reblogsCount,
        repliesCount = currentStatus.repliesCount,
        bookmarked = currentStatus.bookmarked,
        poll = currentStatus.poll
      )
    )
  }

  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.show(it)
    }
  }
}

@Composable
fun StatusDetailTopBar(
  navigationIcon: @Composable () -> Unit,
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  CenterRow(modifier) {
    navigationIcon()
    WidthSpacer(value = 12.dp)
    title()
  }
}

@Composable
fun StatusDetailContent(
  currentStatusId: String,
  statusList: ImmutableList<StatusUiData>,
  lazyState: LazyListState,
  loading: Boolean,
  modifier: Modifier = Modifier,
  action: (StatusAction, String) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToTagInfo: (String) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  val currentStatusIndex by remember(statusList.size) {
    mutableIntStateOf(statusList.indexOfFirst { it.id == currentStatusId })
  }
  val ancestors by remember(statusList) {
    mutableStateOf(statusList.subList(0, currentStatusIndex + 1))
  }
  val descendants by remember(statusList) {
    mutableStateOf((statusList - ancestors).toImmutableList())
  }
  LazyColumn(modifier = modifier, state = lazyState) {
    itemsIndexed(
      items = ancestors,
      key = { _, item -> item.id }
    ) { index, repliedStatus ->
      if (index <= currentStatusIndex) {
        if (repliedStatus.id == currentStatusId) {
          StatusDetailCard(
            status = repliedStatus,
            action = { action(it, repliedStatus.id) },
            navigateToMedia = navigateToMedia,
            navigateToProfile = navigateToProfile,
            navigateToTagInfo = navigateToTagInfo,
            inReply = currentStatusIndex != 0
          )
        } else {
          StatusListItem(
            status = repliedStatus,
            action = { action(it, repliedStatus.id) },
            replyChainType = if (index == 0) Start else Continue,
            hasUnloadedParent = false,
            navigateToDetail = { navigateToDetail(repliedStatus.actionable) },
            navigateToMedia = navigateToMedia,
            navigateToProfile = navigateToProfile,
            navigateToTagInfo = navigateToTagInfo
          )
        }
      }
    }
    item {
      AppHorizontalDivider()
    }
    when (loading) {
      true -> statusLoadingIndicator()
      else -> {
        statusComment(
          descendants = descendants,
          action = action,
          navigateToDetail = navigateToDetail,
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile,
          navigateToTagInfo = navigateToTagInfo
        )
      }
    }
  }
}
