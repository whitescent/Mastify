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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.github.whitescent.mastify.data.repository.InstanceRepository.Companion.DEFAULT_CHARACTER_LIMIT
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
import com.microsoft.fluentui.tokenized.drawer.rememberDrawerState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
  val instanceUiData = state.instance

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
    PostTopBar(
      backButton = {
        ClickableIcon(
          painter = painterResource(id = R.drawable.close),
          modifier = Modifier.size(24.dp),
          tint = AppTheme.colors.primaryContent
        ) {
          navigator.popBackStack()
        }
      },
      title = {
        Text(
          text = "New Moment",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = AppTheme.colors.primaryContent
        )
      },
      action = {
        PostButton(
          enabled = allowPostStatus,
          postState = state.postState,
          post = viewModel::postStatus
        )
      },
      modifier = Modifier
        .statusBarsPadding()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .fillMaxWidth()
    )
    AppHorizontalDivider(Modifier.padding(vertical = 8.dp))
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .background(AppTheme.colors.background),
    ) {
      Column(Modifier.weight(1f)) {
        activeAccount?.let {
          Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            CenterRow {
              CircleShapeAsyncImage(
                model = it.profilePictureUrl,
                modifier = Modifier.size(36.dp),
                shape = AppTheme.shape.smallAvatar
              )
              WidthSpacer(value = 6.dp)
              Column(modifier = Modifier.weight(1f)) {
                HtmlText(
                  text = it.realDisplayName,
                  fontSize = 16.sp,
                  overflow = TextOverflow.Ellipsis,
                  maxLines = 1,
                  fontWeight = FontWeight.Medium,
                  color = AppTheme.colors.primaryContent
                )
                HeightSpacer(value = 2.dp)
                Text(
                  text = it.fullname,
                  color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
                  overflow = TextOverflow.Ellipsis,
                  maxLines = 1,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
        BasicTextField(
          value = postTextField,
          onValueChange = viewModel::updateTextFieldValue,
          modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
          textStyle = TextStyle(fontSize = 18.sp, color = AppTheme.colors.primaryContent),
          cursorBrush = SolidColor(AppTheme.colors.primaryContent),
        ) {
          Column {
            if (viewModel.medias.isNotEmpty()) {
              PostAlbumPanel(
                mediaList = viewModel.medias,
                removeImage = viewModel::removeMedia,
                state = albumRowState
              )
              HeightSpacer(value = 10.dp)
            }
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
          }
        }
      }
      PostToolBar(
        postActionGroup = {
          CenterRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ClickableIcon(
              painter = painterResource(id = R.drawable.image),
              tint = AppTheme.colors.primaryContent,
              modifier = Modifier.size(28.dp),
              onClick = {
                imagePicker.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
              },
            )
            ClickableIcon(
              painter = painterResource(id = R.drawable.emoji),
              tint = AppTheme.colors.primaryContent,
              modifier = Modifier.size(28.dp),
              onClick = { scope.launch { emojiDrawerState.open() } },
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
        },
        textLimitCircle = {
          instanceUiData?.let {
            val progress = postTextField.text.length.toFloat() /
              (it.maximumTootCharacters ?: DEFAULT_CHARACTER_LIMIT).toFloat()
            TextProgressBar(
              textProgress = progress
            ) {
              Text(
                text = buildAnnotatedString {
                  pushStyle(
                    SpanStyle(
                      color = if (postTextField.text.length <= (instanceUiData.maximumTootCharacters ?: DEFAULT_CHARACTER_LIMIT))
                        AppTheme.colors.primaryContent.copy(alpha = 0.48f)
                      else Color(0xFFF53232)
                    )
                  )
                  append("${postTextField.text.length}/${instanceUiData.maximumTootCharacters}")
                  pop()
                }
              )
            }
          }
        },
        visibilityButton = {
          PostVisibilityButton(state.visibility) { scope.launch { visibilitySheetState.open() } }
        },
        modifier = Modifier
          .imePadding()
          .fillMaxWidth()
          .navigationBarsPadding(),
      )
    }
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
    emojis = state.instance?.emojiList?.toImmutableList() ?: persistentListOf(),
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
private fun PostToolBar(
  postActionGroup: @Composable () -> Unit,
  textLimitCircle: @Composable () -> Unit,
  visibilityButton: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  CenterRow(modifier = modifier.padding(vertical = 8.dp)) {
    Box(Modifier.weight(1f)) {
      postActionGroup()
    }
    CenterRow {
      textLimitCircle()
      WidthSpacer(value = 8.dp)
      visibilityButton()
    }
  }
}

@Composable
private fun PostAlbumPanel(
  mediaList: List<MediaModel>,
  removeImage: (Uri?) -> Unit,
  state: LazyListState
) {
  BoxWithConstraints {
    LazyRow(
      state = state,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.heightIn(max = 200.dp)
    ) {
      itemsIndexed(
        items = mediaList,
        key = { _, item -> item.uri.toString() }
      ) { index, media ->
        PostImage(
          mediaModel = media,
          onCancelImage = { removeImage(media.uri) },
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
private fun PostTopBar(
  backButton: @Composable () -> Unit,
  title: @Composable () -> Unit,
  action: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxWidth()) {
    CenterRow {
      Box(Modifier.weight(1f)) { backButton() }
      action()
    }
    Box(Modifier.align(Alignment.Center)) { title() }
  }
}
