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

package com.github.whitescent.mastify.screen.post

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Direct
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Private
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Public
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Unlisted
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.extensions.insertString
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.EmojiSheet
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.viewModel.MediaModel
import com.github.whitescent.mastify.viewModel.PostViewModel
import com.microsoft.fluentui.tokenized.drawer.DrawerState
import com.microsoft.fluentui.tokenized.drawer.rememberDrawerState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@AppNavGraph
@Destination
@Composable
fun Post(
  viewModel: PostViewModel = hiltViewModel(),
  navigator: DestinationsNavigator
) {
  val focusRequester = remember { FocusRequester() }
  var isFocused by remember { mutableStateOf(false) }

  val keyboard = LocalSoftwareKeyboardController.current
  val context = LocalContext.current

  val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
  val allowPostStatus by viewModel.allowPostStatus.collectAsStateWithLifecycle()
  val postTextField = viewModel.postTextField
  val state = viewModel.uiState
  val instanceUiData = state.instanceUiData

  val emojiDrawerState = rememberDrawerState()
  val visibilitySheetState = rememberDrawerState()
  val scope = rememberCoroutineScope()
  val albumRowState = rememberLazyListState()
  val imagePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(4),
    onResult = {
      if (it.size + viewModel.medias.size > 4) {
        Toast.makeText(context, R.string.attachments_exceeded, Toast.LENGTH_LONG).show()
      } else viewModel.addMedia(it)
    }
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background),
  ) {
    PostTopBar(activeAccount) { navigator.popBackStack() }
    AppHorizontalDivider(Modifier.padding(6.dp))
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .weight(1f)
        .background(AppTheme.colors.background),
    ) {
      BasicTextField(
        value = postTextField,
        onValueChange = viewModel::updateTextFieldValue,
        modifier = Modifier
          .fillMaxSize()
          .focusRequester(focusRequester)
          .onFocusChanged { isFocused = it.isFocused },
        textStyle = TextStyle(fontSize = 20.sp, color = AppTheme.colors.primaryContent),
        cursorBrush = SolidColor(AppTheme.colors.primaryContent)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        ) {
          Box {
            if (postTextField.text.isEmpty()) {
              Text(
                text = stringResource(id = R.string.post_placeholder),
                color = Color(0xFFB6B6B6),
                style = TextStyle(fontSize = 18.sp, color = AppTheme.colors.primaryContent),
              )
            }
            it()
          }
          if (viewModel.medias.isNotEmpty()) {
            HeightSpacer(value = 10.dp)
            PostAlbumPanel(
              mediaList = viewModel.medias,
              removeImage = viewModel::removeMedia,
              state = albumRowState
            )
          }
        }
      }
    }
    PostHintBar(
      visibility = state.visibility,
      visibilitySheetState = visibilitySheetState,
      textArea = {
        Text(
          text = buildAnnotatedString {
            pushStyle(
              SpanStyle(
                color = if (postTextField.text.length <= instanceUiData.maximumTootCharacters!!)
                  AppTheme.colors.primaryContent.copy(alpha = 0.48f)
                else Color(0xFFF53232)
              )
            )
            append("${postTextField.text.length}/${instanceUiData.maximumTootCharacters}")
            pop()
          },
        )
        WidthSpacer(value = 2.dp)
        Icon(
          painter = painterResource(id = R.drawable.text),
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = AppTheme.colors.primaryContent.copy(alpha = 0.48f)
        )
      },
      pictureArea = {
        Text(
          text = buildAnnotatedString {
            pushStyle(
              SpanStyle(
                color = if (viewModel.medias.size <= 4)
                  AppTheme.colors.primaryContent.copy(alpha = 0.48f)
                else Color(0xFFF53232)
              )
            )
            append("${viewModel.medias.size}/4")
            pop()
          },
        )
        WidthSpacer(value = 2.dp)
        Icon(
          painter = painterResource(id = R.drawable.images),
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = AppTheme.colors.primaryContent.copy(alpha = 0.48f)
        )
      },
    )
    AppHorizontalDivider()
    PostToolBar(
      modifier = Modifier.padding(12.dp),
      enabledPostButton = allowPostStatus,
      postState = state.postState,
      postStatus = viewModel::postStatus,
      openAlbum = {
        imagePicker.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
      }
    ) { scope.launch { emojiDrawerState.open() } }
  }
  PostVisibilitySheet(
    drawerState = visibilitySheetState,
    currentVisibility = state.visibility,
    onVisibilityUpdated = {
      viewModel.updateVisibility(it)
      scope.launch {
        visibilitySheetState.close()
      }.invokeOnCompletion {
        keyboard?.show()
      }
    }
  )
  EmojiSheet(
    drawerState = emojiDrawerState,
    emojis = state.emojis,
    onSelectEmoji = {
      viewModel.updateTextFieldValue(
        textFieldValue = viewModel.postTextField.copy(
          text = viewModel.postTextField.text.insertString(
            insert = it,
            index = viewModel.postTextField.selection.start
          ),
          selection = TextRange(viewModel.postTextField.selection.start + it.length)
        )
      )
      scope.launch {
        emojiDrawerState.close()
      }.invokeOnCompletion {
        keyboard?.show()
      }
    }
  )
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
  LaunchedEffect(state.postState) {
    if (state.postState is PostState.Success) {
      navigator.popBackStack()
    }
  }
}

@Composable
private fun PostHintBar(
  visibility: Visibility,
  visibilitySheetState: DrawerState,
  textArea: @Composable RowScope.() -> Unit,
  pictureArea: @Composable RowScope.() -> Unit,
) {
  val scope = rememberCoroutineScope()
  CenterRow(Modifier.padding(12.dp)) {
    Box(Modifier.weight(1f)) {
      Surface(
        color = AppTheme.colors.background,
        shape = AppTheme.shape.mediumAvatar,
        border = BorderStroke(1.dp, Color(0xFF777777)),
        onClick = { scope.launch { visibilitySheetState.open() } }
      ) {
        CenterRow(Modifier.padding(vertical = 6.dp, horizontal = 12.dp)) {
          Icon(
            painter = when (visibility) {
              Public -> painterResource(R.drawable.globe)
              Unlisted -> painterResource(R.drawable.lock_open)
              Private -> painterResource(R.drawable.lock)
              Direct -> painterResource(R.drawable.at)
              else -> throw IllegalArgumentException("Invalid visibility")
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF777777)
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = stringResource(
              id = when (visibility) {
                Public -> R.string.public_title
                Unlisted -> R.string.unlisted
                Private -> R.string.private_title
                Direct -> R.string.direct
                else -> throw IllegalArgumentException("Invalid visibility")
              },
            ),
            color = Color(0xFF777777)
          )
        }
      }
    }
    CenterRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      CenterRow {
        textArea()
      }
      CenterRow {
        pictureArea()
      }
    }
  }
}

@Composable
private fun PostAlbumPanel(
  mediaList: List<MediaModel>,
  removeImage: (Int, Uri?) -> Unit,
  state: LazyListState
) {
  BoxWithConstraints {
    LazyRow(
      state = state,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier
        .let {
          if (mediaList.size > 1) it.heightIn(max = 220.dp) else it
        }
    ) {
      itemsIndexed(
        items = mediaList,
        key = { _, item -> item.uri.toString() }
      ) { index, media ->
        PostImage(
          mediaModel = media,
          onCancelImage = { removeImage(index, media.uri) },
          modifier = Modifier
            .let { modifier ->
              if (index > 0) modifier.widthIn(max = 150.dp) else {
                if (mediaList.size > 1) {
                  modifier.widthIn(max = 300.dp)
                } else modifier.widthIn(max = maxWidth)
              }
            }
        )
      }
    }
  }
}

@Composable
private fun PostToolBar(
  enabledPostButton: Boolean,
  postState: PostState,
  modifier: Modifier = Modifier,
  postStatus: () -> Unit,
  openAlbum: () -> Unit,
  openEmojiPicker: () -> Unit,
) {
  CenterRow(
    modifier = modifier
      .imePadding()
      .fillMaxWidth()
      .navigationBarsPadding()
  ) {
    CenterRow(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      ClickableIcon(
        painter = painterResource(id = R.drawable.image),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
        onClick = openAlbum,
      )
      ClickableIcon(
        painter = painterResource(id = R.drawable.emoji),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
        onClick = openEmojiPicker,
      )
      ClickableIcon(
        painter = painterResource(id = R.drawable.warning),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
      )
      ClickableIcon(
        painter = painterResource(id = R.drawable.chart),
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(28.dp),
      )
    }
    IconButton(
      onClick = postStatus,
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = when (postState != PostState.Failure) {
          true -> AppTheme.colors.accent
          else -> Color(0xFFF53232)
        },
        contentColor = Color.White,
        disabledContentColor = Color.Gray
      ),
      enabled = enabledPostButton
    ) {
      when (postState) {
        is PostState.Idle, PostState.Success, PostState.Failure -> {
          Icon(
            painter = painterResource(id = R.drawable.send),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
        }
        is PostState.Posting -> CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
      }
    }
  }
}

@Composable
private fun PostTopBar(
  account: AccountEntity?,
  back: () -> Unit,
) {
  account?.let {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(AppTheme.colors.background)
        .padding(horizontal = 16.dp)
        .padding(vertical = 8.dp)
    ) {
      Spacer(Modifier.statusBarsPadding())
      CenterRow {
        ClickableIcon(
          painter = painterResource(id = R.drawable.close),
          onClick = back,
          modifier = Modifier.size(24.dp),
          tint = AppTheme.colors.primaryContent
        )
        WidthSpacer(value = 6.dp)
        CircleShapeAsyncImage(
          model = account.profilePictureUrl,
          modifier = Modifier.size(36.dp),
          shape = AppTheme.shape.smallAvatar
        )
        WidthSpacer(value = 6.dp)
        Column(modifier = Modifier.weight(1f)) {
          HtmlText(
            text = account.realDisplayName,
            fontSize = 16.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.primaryContent
          )
          HeightSpacer(value = 2.dp)
          Text(
            text = account.fullname,
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}
