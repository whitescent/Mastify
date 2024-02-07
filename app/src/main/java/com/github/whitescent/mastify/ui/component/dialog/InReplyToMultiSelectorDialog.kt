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

package com.github.whitescent.mastify.ui.component.dialog

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.TextWithEmoji
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun InReplyToMultiSelectorDialog(
  dialogState: DialogState,
  threads: List<ReplyThread>,
  onClick: (Int, Boolean) -> Unit
) {
  if (dialogState.show) {
    Dialog(
      onDismissRequest = dialogState::closeDialog,
      properties = DialogProperties(
        usePlatformDefaultWidth = false
      )
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 420.dp)
          .clip(AppTheme.shape.mediumAvatar)
          .background(AppTheme.colors.bottomSheetBackground)
          .padding(horizontal = 28.dp, vertical = 20.dp)
      ) {
        Text(
          text = stringResource(id = R.string.pick_users_title),
          fontSize = 21.sp,
          fontWeight = FontWeight.Bold,
          color = AppTheme.colors.primaryContent,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
          text = stringResource(id = R.string.pick_users_description),
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = AppTheme.colors.cardAction,
          modifier = Modifier.padding(vertical = 16.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          itemsIndexed(threads) { index, item ->
            SelectorItem(
              account = item.account,
              content = item.content,
              selected = item.selected
            ) { onClick(index, !item.selected) }
          }
        }
        HeightSpacer(value = 5.dp)
        Button(
          onClick = dialogState::closeDialog,
          shape = AppTheme.shape.smallAvatar,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF143D73),
            contentColor = Color.White
          ),
          modifier = Modifier.align(Alignment.End)
        ) {
          Text(
            text = stringResource(id = R.string.ok_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    }
  }
}

@Composable
private fun SelectorItem(
  account: Account,
  content: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  val backgroundAnimation by animateColorAsState(
    targetValue = when (selected) {
      true -> AppTheme.colors.dialogItemSelectedBackground
      else -> AppTheme.colors.dialogItemUnselectedBackground
    }
  )
  Surface(
    modifier = Modifier.fillMaxWidth().height(70.dp),
    color = backgroundAnimation,
    shape = AppTheme.shape.mediumAvatar,
    onClick = onClick
  ) {
    CenterRow(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      CircleShapeAsyncImage(
        model = account.avatar,
        modifier = Modifier.size(40.dp),
      )
      Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
        TextWithEmoji(
          text = account.realDisplayName,
          fontSize = 14.sp,
          color = if (selected) Color.White else AppTheme.colors.dialogItemUnselectedContent,
          emojis = account.emojis
        )
        HtmlText(
          text = content,
          fontSize = 12.sp,
          color = if (selected) Color.White else AppTheme.colors.dialogItemUnselectedContent,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      Crossfade(selected) {
        if (it) {
          Icon(
            painter = painterResource(id = R.drawable.check_border_0_5),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.White
          )
        }
      }
    }
  }
}

data class ReplyThread(
  val content: String,
  val account: Account,
  val selected: Boolean
)
