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

package com.github.whitescent.mastify.ui.component.status.paging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.ExplorerKind
import com.github.whitescent.mastify.viewModel.ExplorerKind.PublicTimeline
import com.github.whitescent.mastify.viewModel.ExplorerKind.Trending

@Composable
fun EmptyStatusListPlaceholder(
  pagePlaceholderType: PagePlaceholderType,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_status))
  val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
  Box(
    modifier = modifier,
    contentAlignment = alignment
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(300.dp),
        contentScale = ContentScale.Crop
      )
      Text(
        text = stringResource(
          id = when (pagePlaceholderType) {
            is PagePlaceholderType.Home -> R.string.empty_timeline
            is PagePlaceholderType.Profile -> {
              if (pagePlaceholderType.isSelf) R.string.empty_status_self
              else R.string.empty_status
            }
            is PagePlaceholderType.Explore -> {
              when (pagePlaceholderType.explorerKind) {
                PublicTimeline, Trending -> R.string.timeline_is_empty
                ExplorerKind.News -> R.string.news_is_empty
              }
            }
          }
        ),
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.cardMenu,
        fontSize = 18.sp,
      )
    }
  }
}

sealed class PagePlaceholderType {
  data object Home : PagePlaceholderType()
  data class Profile(val isSelf: Boolean) : PagePlaceholderType()
  data class Explore(val explorerKind: ExplorerKind) : PagePlaceholderType()
}
