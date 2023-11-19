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

package com.github.whitescent.mastify.ui.component.status

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.StatusAction

@Composable
fun StatusDropdownMenu(
  expanded: Boolean,
  enableCopyText: Boolean,
  statusUiData: StatusUiData,
  modifier: Modifier = Modifier,
  offset: IntOffset = IntOffset.Zero,
  onDismissRequest: () -> Unit,
  actionHandler: (StatusAction) -> Unit
) {
  var bookmarkState by remember(statusUiData.bookmarked) {
    mutableStateOf(statusUiData.bookmarked)
  }
  if (expanded) {
    val actions = mutableListOf<MenuAction>().apply {
      if (enableCopyText)
        add(
          MenuAction(
            text = R.string.copy_text,
            icon = R.drawable.copy,
            action = StatusAction.CopyText(statusUiData.parsedContent)
          )
        )
      add(
        MenuAction(
          text = R.string.copy_link,
          icon = R.drawable.link_simple,
          action = StatusAction.CopyLink(statusUiData.link)
        )
      )
      add(
        MenuAction(
          text = if (bookmarkState) R.string.delete_bookmark else R.string.bookmark,
          icon = if (bookmarkState) R.drawable.bookmark_fill else R.drawable.bookmark_simple,
          action = StatusAction.Bookmark(
            id = statusUiData.actionableId,
            bookmark = !bookmarkState
          )
        )
      )
      add(MenuAction(R.string.mute_account, R.drawable.eye_hide, StatusAction.Mute))
      add(MenuAction(R.string.block_account, R.drawable.block, StatusAction.Block))
      add(MenuAction(R.string.report, R.drawable.report, StatusAction.Report))
    }
    Popup(
      onDismissRequest = onDismissRequest,
      properties = PopupProperties(true),
      offset = offset,
      alignment = Alignment.TopEnd,
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.background,
        shadowElevation = 6.dp,
        modifier = modifier.widthIn(max = 250.dp)
      ) {
        Column {
          actions.forEach {
            StatusMenuListItem(
              action = it,
              targetActionFullname = when (it.textHasArgs) {
                true -> statusUiData.fullname
                else -> null
              }
            ) {
              if (it.action is StatusAction.Bookmark) bookmarkState = !bookmarkState
              actionHandler(it.action)
            }
            if (it != actions.last()) AppHorizontalDivider()
          }
        }
      }
    }
  }
}

@Composable
private fun StatusMenuListItem(
  action: MenuAction,
  targetActionFullname: String?,
  onClick: () -> Unit
) {
  CenterRow(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        onClick = onClick,
        indication = rememberRipple(
          bounded = true,
          radius = 250.dp,
        ),
        interactionSource = remember { MutableInteractionSource() },
      )
      .padding(horizontal = 20.dp, vertical = 16.dp)
  ) {
    Icon(
      painter = painterResource(id = action.icon),
      contentDescription = null,
      tint = if (action.isNormalAction) AppTheme.colors.primaryContent else Color(0xFFE75656),
      modifier = Modifier.size(22.dp)
    )
    WidthSpacer(value = 8.dp)
    Text(
      text = stringResource(id = action.text, targetActionFullname ?: ""),
      color = if (action.isNormalAction) AppTheme.colors.primaryContent else Color(0xFFE75656),
      fontSize = 16.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

private data class MenuAction(
  @StringRes val text: Int,
  @DrawableRes val icon: Int,
  val action: StatusAction
) {
  val isNormalAction get() = this.action != StatusAction.Report
  val textHasArgs get() = this.action is StatusAction.Mute || this.action is StatusAction.Block
}
