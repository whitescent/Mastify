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

package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.extensions.getSizeOfIndex
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.clickableWithoutIndication
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiSheet(
  sheetState: SheetState,
  emojis: ImmutableList<Emoji>,
  onSelectEmoji: (String) -> Unit,
  onDismissRequest: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val lazyGridState = rememberLazyGridState()
  ModalBottomSheet(
    sheetState = sheetState,
    windowInsets = WindowInsets.statusBars,
    onDismissRequest = onDismissRequest,
    containerColor = AppTheme.colors.bottomSheetBackground
  ) {
    val categorizedEmojis by remember(emojis) {
      mutableStateOf(emojis.filter { it.category != null })
    }
    val uncategorizedEmojis by remember(emojis) {
      mutableStateOf(emojis.filter { it.category == null })
    }
    val emojiGroup by remember(categorizedEmojis) {
      mutableStateOf(categorizedEmojis.groupBy { it.category })
    }
    Column(Modifier.navigationBarsPadding()) {
      if (categorizedEmojis.isNotEmpty()) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(24.dp),
          contentPadding = PaddingValues(24.dp),
        ) {
          emojiGroup.onEachIndexed { index, (categoryName, emoji) ->
            categoryName?.let {
              item {
                AsyncImage(
                  model = ImageRequest.Builder(LocalContext.current)
                    .data(emoji[0].url)
                    .crossfade(true)
                    .build(),
                  contentDescription = null,
                  modifier = Modifier
                    .clickableWithoutIndication {
                      scope.launch {
                        lazyGridState.scrollToItem(emojiGroup.getSizeOfIndex(index))
                      }
                    }
                    .size(24.dp)
                )
              }
            }
          }
        }
        HorizontalDivider()
      }
      LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(24.dp),
        state = lazyGridState
      ) {
        if (uncategorizedEmojis.isNotEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
              text = stringResource(id = R.string.uncategorized_title),
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              modifier = Modifier.fillMaxWidth(),
              color = AppTheme.colors.primaryContent,
            )
          }
          items(
            items = uncategorizedEmojis,
            contentType = { it.itemType },
            key = { it.url }
          ) {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(it.url)
                .crossfade(true)
                .build(),
              contentDescription = null,
              modifier = Modifier.size(32.dp).clickable { onSelectEmoji(" :${it.shortcode}: ") },
            )
          }
        }
        emojiGroup.forEach { (category, emoji) ->
          category?.let {
            item(
              span = { GridItemSpan(maxLineSpan) }
            ) {
              Text(
                text = category,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.primaryContent
              )
            }
            items(
              items = emoji,
              contentType = { it.itemType },
              key = { it.url }
            ) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(it.url)
                  .crossfade(true)
                  .build(),
                contentDescription = null,
                modifier = Modifier.size(32.dp).clickable { onSelectEmoji(" :${it.shortcode}: ") }
              )
            }
          }
        }
      }
    }
  }
}
