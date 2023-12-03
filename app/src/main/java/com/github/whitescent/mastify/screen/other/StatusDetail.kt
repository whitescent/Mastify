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

package com.github.whitescent.mastify.screen.other

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.extensions.insertString
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.EmojiSheet
import com.github.whitescent.mastify.ui.component.ReplyTextField
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.component.status.StatusDetailCard
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.rememberStatusSnackBarState
import com.github.whitescent.mastify.ui.component.statusComment
import com.github.whitescent.mastify.ui.component.statusLoadingIndicator
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.viewModel.StatusDetailViewModel
import com.microsoft.fluentui.tokenized.drawer.rememberDrawerState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

data class StatusDetailNavArgs(
  val status: Status,
  val originStatusId: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@AppNavGraph
@Destination(
  navArgsDelegate = StatusDetailNavArgs::class
)
@Composable
fun StatusDetail(
  navigator: DestinationsNavigator,
  viewModel: StatusDetailViewModel = hiltViewModel()
) {
  val lazyState = rememberLazyListState()
  val drawerState = rememberDrawerState()
  val scope = rememberCoroutineScope()
  val snackbarState = rememberStatusSnackBarState()
  val keyboard = LocalSoftwareKeyboardController.current
  val context = LocalContext.current

  val state = viewModel.uiState
  val replyText = viewModel.replyField

  val currentStatus = viewModel.currentStatus
  val threadInReply = currentStatus.reblog?.isInReplyTo ?: currentStatus.isInReplyTo

  Box((Modifier.fillMaxSize())) {
    Column {
      Spacer(Modifier.statusBarsPadding())
      CenterRow(Modifier.padding(12.dp)) {
        ClickableIcon(
          painter = painterResource(id = R.drawable.arrow_left),
          onClick = { navigator.popBackStack() },
          modifier = Modifier.size(28.dp),
          tint = AppTheme.colors.primaryContent
        )
        WidthSpacer(value = 12.dp)
        Text(
          text = stringResource(id = R.string.home_title),
          fontSize = 20.sp,
          fontWeight = FontWeight.Medium,
          color = AppTheme.colors.primaryContent,
        )
      }
      AppHorizontalDivider(Modifier.padding(vertical = 6.dp))
      when (threadInReply) {
        true -> {
          StatusDetailInReply(
            status = currentStatus,
            lazyState = lazyState,
            ancestors = state.ancestors.toImmutableList(),
            descendants = state.descendants.toImmutableList(),
            loading = state.loading,
            action = {
              viewModel.onStatusAction(it, context)
            },
            navigateToDetail = {
              if (it.id != currentStatus.actionableId) {
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
            modifier = Modifier.weight(1f)
          )
        }
        else -> {
          StatusDetailContent(
            status = currentStatus,
            lazyState = lazyState,
            descendants = state.descendants.toImmutableList(),
            loading = state.loading,
            action = {
              viewModel.onStatusAction(it, context)
            },
            navigateToDetail = {
              if (it.id != currentStatus.actionableId) {
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
            navigateToProfile = { navigator.navigate(ProfileDestination(it)) },
            modifier = Modifier.weight(1f),
          )
        }
      }
      ReplyTextField(
        targetAccount = viewModel.navArgs.status.account,
        fieldValue = replyText,
        postState = state.postState,
        onValueChange = viewModel::updateTextFieldValue,
        replyToStatus = viewModel::replyToStatus,
        openEmojiPicker = { scope.launch { drawerState.open() } }
      )
    }
    StatusSnackBar(
      snackbarState = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 12.dp, end = 12.dp, bottom = 56.dp)
    )
  }

  EmojiSheet(
    drawerState = drawerState,
    emojis = state.instanceEmojis,
    onSelectEmoji = {
      viewModel.updateTextFieldValue(
        textFieldValue = viewModel.replyField.copy(
          text = viewModel.replyField.text.insertString(it, viewModel.replyField.selection.start),
          selection = TextRange(viewModel.replyField.selection.start + it.length)
        )
      )
      scope.launch {
        drawerState.close()
      }.invokeOnCompletion {
        keyboard?.show()
      }
    }
  )

  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.show(it)
    }
  }
}

@Composable
fun StatusDetailContent(
  status: StatusUiData,
  lazyState: LazyListState,
  descendants: ImmutableList<StatusUiData>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  action: (StatusAction) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .drawVerticalScrollbar(lazyState),
    state = lazyState
  ) {
    item {
      StatusDetailCard(
        status = status,
        action = action,
        navigateToMedia = navigateToMedia,
        navigateToProfile = navigateToProfile
      )
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
          navigateToProfile = navigateToProfile
        )
      }
    }
  }
}

@Composable
fun StatusDetailInReply(
  status: StatusUiData,
  lazyState: LazyListState,
  ancestors: ImmutableList<StatusUiData>,
  descendants: ImmutableList<StatusUiData>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  action: (StatusAction) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  LazyColumn(modifier = modifier, state = lazyState) {
    itemsIndexed(
      items = ancestors + status,
      key = { _, item -> item.id }
    ) { index, repliedStatus ->
      if (repliedStatus == status) {
        StatusDetailCard(
          status = status,
          action = action,
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile,
          inReply = true
        )
      } else {
        StatusListItem(
          status = repliedStatus,
          action = action,
          replyChainType = if (index == 0) Start else Continue,
          hasUnloadedParent = false,
          navigateToDetail = { navigateToDetail(repliedStatus.actionable) },
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile
        )
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
          navigateToProfile = navigateToProfile
        )
      }
    }
  }
}
