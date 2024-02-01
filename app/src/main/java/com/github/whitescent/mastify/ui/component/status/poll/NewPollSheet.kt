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

package com.github.whitescent.mastify.ui.component.status.poll

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import com.github.whitescent.R
import com.github.whitescent.mastify.data.repository.InstanceRepository.Companion.DEFAULT_MAX_OPTION_COUNT
import com.github.whitescent.mastify.data.repository.InstanceRepository.Companion.DEFAULT_MAX_OPTION_LENGTH
import com.github.whitescent.mastify.database.model.InstanceEntity
import com.github.whitescent.mastify.extensions.buildTextWithLimit
import com.github.whitescent.mastify.screen.post.TextProgressBar
import com.github.whitescent.mastify.ui.component.AppHorizontalDashedDivider
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.OptionSwitchButton
import com.github.whitescent.mastify.ui.component.PollSwitchOption
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.pollDurationList
import com.github.whitescent.mastify.viewModel.VoteType
import com.microsoft.fluentui.tokenized.drawer.BottomDrawer
import com.microsoft.fluentui.tokenized.drawer.rememberDrawerState
import kotlinx.coroutines.launch

@Composable
fun NewPollSheet(
  instanceData: InstanceEntity?,
  optionList: SnapshotStateList<TextFieldValue>,
  close: () -> Unit,
  onTextFieldFocusChange: (Int) -> Unit,
  onDurationChange: (Int) -> Unit,
  onPollListChange: (List<TextFieldValue>) -> Unit,
  onPollValidChange: (Boolean) -> Unit,
  onPollTypeChange: (Int) -> Unit,
) {
  val maxPollOptions = instanceData?.maxPollOptions ?: DEFAULT_MAX_OPTION_COUNT
  val maxPollLength = instanceData?.maxPollCharactersPerOption ?: DEFAULT_MAX_OPTION_LENGTH

  val keyboard = LocalSoftwareKeyboardController.current
  val deadlineDrawerState = rememberDrawerState()
  val scope = rememberCoroutineScope()

  var selectedIndex by remember { mutableStateOf(4) }

  val isPollListValid by remember {
    derivedStateOf {
      val hasInvalidLength = optionList.any { it.text.isEmpty() || it.text.length > maxPollLength }
      val hasDuplicates = optionList.groupingBy { it }.eachCount().any { it.value > 1 }
      !hasInvalidLength && !hasDuplicates
    }
  }

  Box(
    modifier = Modifier
      .clip(AppTheme.shape.mediumAvatar)
      .border(1.dp, Color(0xFFE5AB00), AppTheme.shape.mediumAvatar)
  ) {
    Column {
      CenterRow(
        Modifier
          .background(Color(0xFFFDF4D9))
          .padding(14.dp)) {
        CenterRow(Modifier.weight(1f)) {
          Image(
            painter = painterResource(id = R.drawable.clock),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = stringResource(
              id = R.string.poll_close_time,
              stringResource(id = pollDurationList[selectedIndex].text)
            ),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(40, 44, 48),
          )
        }
        ClickableIcon(
          painter = painterResource(id = R.drawable.close),
          tint = Color.Gray,
          onClick = close
        )
      }
      AppHorizontalDashedDivider(Modifier.fillMaxWidth())
      Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        optionList.forEachIndexed { index, textFieldValue ->
          Column {
            PollOptionTextField(
              index = index + 1,
              textFieldValue = textFieldValue,
              maxTextLength = maxPollLength,
              onValueChanged = {
                optionList[index] = it
                onPollListChange(optionList)
              },
              onRemoveOption = { optionList.removeAt(index) },
              showCloseButton = optionList.size > 2,
              duplicated = optionList.count { it == textFieldValue } > 1 &&
                textFieldValue.text.isNotEmpty(),
              modifier = Modifier
                .weight(1f)
                .onFocusChanged {
                  if (it.isFocused) {
                    onTextFieldFocusChange(index)
                  }
                }
            )
            if (index != optionList.lastIndex) { HeightSpacer(value = 6.dp) }
          }
        }
        CenterRow(Modifier.padding(vertical = 10.dp)) {
          CenterRow(modifier = Modifier.weight(1f)) {
            Icon(
              painter = painterResource(id = R.drawable.chart),
              contentDescription = null,
              tint = AppTheme.colors.primaryContent.copy(0.7f),
              modifier = Modifier.size(22.dp)
            )
            WidthSpacer(value = 3.dp)
            Text(
              text = stringResource(id = R.string.poll_type),
              color = AppTheme.colors.primaryContent.copy(0.7f),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
            )
          }
          OptionSwitchButton(
            options = VoteType.entries.map {
              PollSwitchOption(
                text = stringResource(id = it.text),
                icon = painterResource(id = it.icon),
              )
            },
            onClick = onPollTypeChange,
          )
        }
        Button(
          onClick = {
            scope.launch {
              keyboard?.hide()
              deadlineDrawerState.open()
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = AppTheme.shape.betweenSmallAndMediumAvatar,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9F69E4)
          ),
        ) {
          Icon(
            painter = painterResource(id = R.drawable.calendar),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White,
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = stringResource(id = R.string.poll_deadlines),
            color = Color.White,
          )
        }
        HeightSpacer(value = 4.dp)
        Button(
          onClick = { optionList.add(TextFieldValue()) },
          enabled = optionList.size < maxPollOptions,
          modifier = Modifier.fillMaxWidth(),
          shape = AppTheme.shape.betweenSmallAndMediumAvatar,
          colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accent
          ),
        ) {
          Text(
            text = stringResource(id = R.string.add_poll_option),
            color = Color.White,
          )
        }
      }
    }
  }

  LaunchedEffect(isPollListValid) {
    onPollValidChange(isPollListValid)
  }

  BottomDrawer(
    drawerContent = {
      Column(Modifier.padding(vertical = 14.dp)) {
        CenterRow(Modifier.padding(horizontal = 14.dp)) {
          Icon(
            painter = painterResource(id = R.drawable.calendar),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = AppTheme.colors.primaryContent
          )
          WidthSpacer(value = 6.dp)
          Text(
            text = stringResource(id = R.string.poll_deadlines),
            color = AppTheme.colors.primaryContent,
            fontSize = 24.sp
          )
        }
        AppHorizontalDivider(Modifier.padding(vertical = 12.dp))
        pollDurationList.forEachIndexed { index, pollDuration ->
          CenterRow(
            modifier = Modifier
              .clickable {
                selectedIndex = index
                onDurationChange(pollDuration.duration)
                scope.launch {
                  deadlineDrawerState.close()
                }
              }
              .padding(horizontal = 14.dp)
          ) {
            Text(
              text = stringResource(id = pollDuration.text),
              color = AppTheme.colors.primaryContent,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.weight(1f)
            )
            RadioButton(
              selected = index == selectedIndex,
              onClick = {
                selectedIndex = index
                onDurationChange(pollDuration.duration)
                scope.launch {
                  deadlineDrawerState.close()
                }
              },
              colors = RadioButtonDefaults.colors(
                selectedColor = AppTheme.colors.primaryContent,
              ),
            )
          }
        }
      }
    },
    drawerState = deadlineDrawerState,
    windowInsetsType = WindowInsetsCompat.Type.statusBars(),
  )
}

@Composable
private fun PollOptionTextField(
  index: Int,
  textFieldValue: TextFieldValue,
  maxTextLength: Int,
  onValueChanged: (TextFieldValue) -> Unit,
  onRemoveOption: () -> Unit,
  modifier: Modifier = Modifier,
  showCloseButton: Boolean = false,
  duplicated: Boolean = false,
) {
  Column {
    CenterRow {
      BasicTextField(
        value = textFieldValue.copy(
          annotatedString = textFieldValue.text.buildTextWithLimit(
            maxLength = maxTextLength,
            textColor = AppTheme.colors.primaryContent,
            warningBackgroundColor = AppTheme.colors.textLimitWarningBackground
          )
        ),
        onValueChange = onValueChanged,
        singleLine = true,
        textStyle = TextStyle(color = AppTheme.colors.primaryContent),
        cursorBrush = SolidColor(AppTheme.colors.primaryContent),
        modifier = modifier
      ) {
        Box(
          modifier = Modifier
            .border(
              width = 1.dp,
              color = if (!duplicated) AppTheme.colors.accent else Color(0xFFF53232),
              shape = AppTheme.shape.betweenSmallAndMediumAvatar
            )
            .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
        ) {
          CenterRow(
            Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Column {
              CenterRow {
                Text(
                  text = stringResource(id = R.string.poll_option, index),
                  fontSize = 12.sp,
                  modifier = Modifier.weight(1f),
                  color = Color.Gray,
                )
                TextProgressBar(
                  textLength = textFieldValue.text.length,
                  maxTextLength = maxTextLength,
                  modifier = Modifier.size(16.dp),
                  fontSize = 12.sp
                )
              }
              it()
            }
          }
        }
      }
      Crossfade(showCloseButton) {
        if (it) {
          ClickableIcon(
            painter = painterResource(id = R.drawable.close_circle),
            modifier = Modifier.padding(start = 16.dp, end = 2.dp),
            interactiveSize = 26.dp,
            tint = Color.Gray,
            onClick = onRemoveOption
          )
        }
      }
    }
    Crossfade(targetState = duplicated) {
      if (it) {
        Column {
          HeightSpacer(value = 4.dp)
          Text(
            text = stringResource(id = R.string.duplicate_poll_option),
            color = Color(0xFFF53232),
            fontSize = 12.sp,
          )
        }
      }
    }
  }
}
