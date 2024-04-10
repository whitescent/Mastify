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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
import com.github.whitescent.mastify.ui.component.status.poll.NewPollSheet
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.viewModel.MediaModel
import com.github.whitescent.mastify.viewModel.PostViewModel
import com.github.whitescent.mastify.viewModel.VoteType.Multiple
import com.github.whitescent.mastify.viewModel.VoteType.Single
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@AppNavGraph
@Destination
@Composable
fun Post(
  viewModel: PostViewModel = hiltViewModel(),
  navigator: DestinationsNavigator
) {
  val focusRequester = remember { FocusRequester() }
  var isEditorFocused by remember { mutableStateOf(false) }

  val pollOptionList = remember { mutableStateListOf(TextFieldValue(), TextFieldValue()) }
  var focusedPollOptionIndex by remember { mutableIntStateOf(0) }

  val keyboard = LocalSoftwareKeyboardController.current
  val context = LocalContext.current

  val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
  val allowPostStatus by viewModel.allowPostStatus.collectAsStateWithLifecycle()
  val state = viewModel.uiState
  val instanceUiData = state.instance

  var openVisibilitySheet by remember { mutableStateOf(false) }
  val visibilitySheetState = rememberModalBottomSheetState(
    // Always fully expand the visibility sheet even on small screens
    skipPartiallyExpanded = true
  )
  var openEmojiSheet by remember { mutableStateOf(false) }
  val emojiSheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  val albumRowState = rememberLazyListState()
  val textFieldState = viewModel.postTextField
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
          text = stringResource(id = R.string.new_moment),
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
      Column(
        Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState())) {
        activeAccount?.let {
          Column(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)) {
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
        if (viewModel.medias.isNotEmpty()) {
          PostAlbumPanel(
            mediaList = viewModel.medias,
            removeImage = viewModel::removeMedia,
            state = albumRowState
          )
          HeightSpacer(value = 10.dp)
        }
        if (state.pollListModel.showPoll) {
          NewPollSheet(
            instanceData = instanceUiData,
            optionList = pollOptionList,
            close = {
              viewModel.updatePollListModel(state.pollListModel.copy(showPoll = false))
              focusRequester.requestFocus()
            },
            onPollListChange = {
              viewModel.updatePollListModel(state.pollListModel.copy(list = it))
            },
            onPollValidChange = {
              viewModel.updatePollListModel(state.pollListModel.copy(isPollListValid = it))
            },
            onPollTypeChange = {
              viewModel.updatePollListModel(
                state.pollListModel.copy(
                  voteType = if (it == Single.ordinal) Single else Multiple
                )
              )
            },
            onDurationChange = {
              viewModel.updatePollListModel(state.pollListModel.copy(duration = it))
            },
            onTextFieldFocusChange = {
              focusedPollOptionIndex = it
            }
          )
          HeightSpacer(value = 10.dp)
        }
        BasicTextField(
          state = textFieldState,
          modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onFocusChanged { isEditorFocused = it.isFocused },
          outputTransformation = {
            // TODO: If BF2 supports AnnotatedString, replace this
            // buildAnnotatedString {
            //   textFieldState.text.toString().buildTextWithLimit(
            //     maxLength = state.instance?.maximumTootCharacters ?: DEFAULT_CHARACTER_LIMIT,
            //     textColor = primaryContent,
            //     warningBackgroundColor = warningBackgroundColor
            //   )
            // }
          },
          textStyle = TextStyle(fontSize = 18.sp, color = AppTheme.colors.primaryContent),
          cursorBrush = SolidColor(AppTheme.colors.primaryContent),
          decorator = {
            Box {
              if (textFieldState.text.isEmpty()) {
                Text(
                  text = stringResource(id = R.string.post_placeholder),
                  color = Color(0xFFB6B6B6),
                  style = TextStyle(fontSize = 18.sp, color = AppTheme.colors.primaryContent),
                )
              }
              it()
            }
          }
        )
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
              enabled = !state.pollListModel.showPoll
            )
            ClickableIcon(
              painter = painterResource(id = R.drawable.emoji),
              tint = AppTheme.colors.primaryContent,
              modifier = Modifier.size(28.dp),
              onClick = {
                scope.launch {
                  keyboard?.hide()
                  openEmojiSheet = true
                }
              },
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
              enabled = viewModel.medias.isEmpty()
            ) {
              viewModel.updatePollListModel(
                pollListModel = state.pollListModel.copy(showPoll = !state.pollListModel.showPoll)
              )
              focusRequester.requestFocus()
            }
          }
        },
        textLimitCircle = {
          instanceUiData?.let {
            TextProgressBar(
              textLength = textFieldState.text.length,
              maxTextLength = instanceUiData.maximumTootCharacters ?: DEFAULT_CHARACTER_LIMIT
            )
          }
        },
        visibilityButton = {
          PostVisibilityButton(state.visibility) {
            scope.launch {
              openVisibilitySheet = true
            }
          }
        },
        modifier = Modifier
          .imePadding()
          .fillMaxWidth()
          .navigationBarsPadding(),
      )
    }
  }
  if (openVisibilitySheet) {
    PostVisibilitySheet(
      sheetState = visibilitySheetState,
      currentVisibility = state.visibility,
      onVisibilityUpdated = {
        viewModel.updateVisibility(it)
        scope.launch {
          visibilitySheetState.hide()
        }.invokeOnCompletion {
          keyboard?.show()
          openVisibilitySheet = false
        }
      },
      onDismissRequest = {
        openVisibilitySheet = false
      }
    )
  }
  if (openEmojiSheet) {
    EmojiSheet(
      sheetState = emojiSheetState,
      emojis = state.instance?.emojiList?.toImmutableList() ?: persistentListOf(),
      onSelectEmoji = {
        when (isEditorFocused) {
          true -> {
            textFieldState.edit {
              insert(selection.start, it)
            }
          }
          false -> {
            val textFieldValue = pollOptionList[focusedPollOptionIndex]
            pollOptionList[focusedPollOptionIndex] = textFieldValue.copy(
              text = textFieldValue.text.insertString(
                insert = it,
                index = textFieldValue.selection.start
              ),
              selection = TextRange(textFieldValue.selection.start + it.length)
            )
          }
        }
        scope.launch {
          emojiSheetState.hide()
        }.invokeOnCompletion {
          keyboard?.show()
          openEmojiSheet = false
        }
      },
      onDismissRequest = { openEmojiSheet = false }
    )
  }
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
        UploadImage(
          mediaModel = media,
          onCancelImage = { removeImage(media.uri) },
          modifier = Modifier
            .let { modifier ->
              when (index > 0) {
                true -> modifier.widthIn(max = 150.dp)
                false -> {
                  if (mediaList.size > 1) {
                    modifier.widthIn(max = 300.dp)
                  } else {
                    modifier.widthIn(max = maxWidth)
                  }
                }
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
