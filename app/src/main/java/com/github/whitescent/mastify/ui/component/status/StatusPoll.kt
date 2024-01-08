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

package com.github.whitescent.mastify.ui.component.status

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.status.Poll
import com.github.whitescent.mastify.network.model.status.Poll.PollOption
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.theme.shape.SmoothCornerShape
import com.github.whitescent.mastify.utils.FormatFactory.getPercentageString
import com.github.whitescent.mastify.utils.formatDurationUntilEnd

@Composable
fun StatusPoll(
  poll: Poll?,
  modifier: Modifier = Modifier,
  onVotePoll: (String, List<Int>) -> Unit
) {
  poll?.let {
    val options = it.options
    val allowVote by remember(poll.expired, poll.voted) {
      mutableStateOf(!poll.expired && !poll.voted)
    }
    var selectedOption by remember(it) { mutableStateOf<Int?>(null) }
    var selectedOptions by remember(it) { mutableStateOf(setOf<Int>()) }
    Column(modifier = modifier) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { index, pollOption ->
          PollOption(
            votesCount = poll.votesCount,
            pollOption = pollOption,
            multiple = poll.multiple,
            allowVote = allowVote,
            selected = when (allowVote) {
              true -> {
                when (poll.multiple) {
                  true -> selectedOptions.contains(index)
                  else -> selectedOption == index
                }
              }
              false -> poll.ownVotes?.contains(index) ?: false
            },
          ) { selected ->
            when (poll.multiple) {
              true -> {
                val sets = selectedOptions.toMutableSet()
                if (selected) sets.add(index) else sets.remove(index)
                selectedOptions = sets
              }
              false -> selectedOption = index
            }
          }
        }
      }
      HeightSpacer(6.dp)
      CenterRow {
        if (poll.votersCount != null && poll.votersCount > 0) {
          Text(
            text = stringResource(R.string.vote_poll_participants_count, poll.votersCount),
            fontSize = 12.sp,
            color = Color(0xFFA1A1A1),
            modifier = Modifier.weight(1f)
          )
        }
        Text(
          text = formatDurationUntilEnd(poll.expiresAt),
          fontSize = 12.sp,
          color = Color(0xFFA1A1A1),
          fontWeight = FontWeight.Bold
        )
      }
      Crossfade(selectedOption != null || selectedOptions.isNotEmpty()) { showButton ->
        if (showButton) {
          Column {
            HeightSpacer(4.dp)
            Button(
              onClick = {
                onVotePoll(
                  poll.id,
                  if (poll.multiple) selectedOptions.toList() else listOf(selectedOption!!)
                )
              },
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(8.dp),
              shape = SmoothCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF046FFF),
                contentColor = Color.White
              ),
            ) {
              Text(
                text = stringResource(id = R.string.vote_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PollOption(
  votesCount: Int,
  pollOption: PollOption,
  multiple: Boolean,
  allowVote: Boolean,
  selected: Boolean,
  onSelected: (Boolean) -> Unit
) {
  val pollOptionAnimatable by animateFloatAsState(
    targetValue = if (votesCount > 0 && !allowVote)
      pollOption.votesCount.toFloat() / votesCount.toFloat() else 0f,
    animationSpec = tween(800, delayMillis = 500, easing = EaseOutQuart)
  )
  Box(
    modifier = Modifier
      .height(IntrinsicSize.Min)
      .let {
        if (allowVote)
          it.border(1.dp, AppTheme.colors.replyTextFieldBorder, AppTheme.shape.voteOption)
        else it
      }
      .clip(SmoothCornerShape(12.dp))
      .clickable(enabled = allowVote) { onSelected(!selected) }
  ) {
    if (!allowVote) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(AppTheme.colors.pollOptionBackground, AppTheme.shape.voteOption)
      )
      PollProgressBar(
        modifier = Modifier.fillMaxHeight(),
        progress = pollOptionAnimatable
      )
    }
    CenterRow(Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 22.dp)) {
      CenterRow(Modifier.weight(1f)) {
        if (!allowVote && selected) {
          Image(
            painter = painterResource(id = R.drawable.check_circle_fill),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          WidthSpacer(value = 6.dp)
        }
        Text(
          text = pollOption.title,
          color = AppTheme.colors.primaryContent,
          fontSize = 16.sp
        )
      }
      WidthSpacer(value = 6.dp)
      when (allowVote) {
        true -> {
          when (multiple) {
            true -> {
              Checkbox(
                checked = selected,
                onCheckedChange = onSelected,
                modifier = Modifier.size(32.dp),
                colors = CheckboxDefaults.colors(
                  checkedColor = Color(0xFF17C252).copy(0.8f)
                )
              )
            }
            else -> {
              AnimatedContent(
                targetState = selected,
                transitionSpec = {
                  scaleIn() togetherWith scaleOut()
                }
              ) { selected ->
                when (selected) {
                  true -> Image(
                    painter = painterResource(id = R.drawable.check_circle_fill),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                  )
                  false -> Box(Modifier.size(32.dp).padding(4.dp).border(1.5.dp, Color.Gray, CircleShape))
                }
              }
            }
          }
        }
        false -> {
          Column(
            modifier = Modifier.height(32.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              text = if (votesCount > 0)
                getPercentageString(pollOption.votesCount.toFloat() / votesCount.toFloat())
              else "0%",
              fontSize = 12.sp,
              color = AppTheme.colors.primaryContent.copy(0.6f)
            )
            if (pollOption.votesCount > 0) {
              Text(
                text = pluralStringResource(
                  R.plurals.votes,
                  pollOption.votesCount,
                  pollOption.votesCount
                ),
                fontSize = 12.sp,
                color = AppTheme.colors.primaryContent.copy(0.6f)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PollProgressBar(
  modifier: Modifier = Modifier,
  color: Color = Color(0xFF187CFF).copy(alpha = 0.75f),
  progress: Float
) = Box(
  modifier = modifier
    .fillMaxWidth(progress)
    .background(color = color, shape = AppTheme.shape.voteOption)
)
