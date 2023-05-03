package com.github.whitescent.mastify.ui.component.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.whitescent.R
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.FormatFactory

@Composable
fun StatusContent(
  avatar: String,
  displayName: String,
  username: String,
  instanceName: String,
  createdAt: String,
  content: String,
  mediaAttachments: List<MediaAttachments>,
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int
) {
  
  val actionList = mapOf(
    R.drawable.chat_3_line to repliesCount,
    R.drawable.repeat_2_line to reblogsCount,
    R.drawable.heart_3_line to favouritesCount
  )

  var openDialog by rememberSaveable { mutableStateOf(false) }
  var media by remember { mutableStateOf<List<MediaAttachments>>(listOf()) }
  var targetMediaIndex by remember { mutableStateOf(0) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(12.dp)
  ) {
    CircleShapeAsyncImage(
      model = avatar,
      modifier = Modifier.size(50.dp)
    )
    WidthSpacer(value = 6.dp)
    Column {
      CenterRow(
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = displayName,
            style = AppTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
          Text(
            text = "@$username@$instanceName",
            style = AppTheme.typography.titleSmall,
            color = AppTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
        }
        CenterRow(modifier = Modifier.padding(start = 8.dp)) {
          Text(
            text = FormatFactory.getTimeDiff(createdAt),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onBackground.copy(alpha = 0.5f)
          )
          WidthSpacer(value = 4.dp)
          ClickableIcon(
            painter = painterResource(id = R.drawable.more_2_fill),
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      HeightSpacer(value = 4.dp)
      HtmlText(
        htmlText = content,
        textStyle = AppTheme.typography.titleSmall,
        openLink = { },
        maxLines = 11,
        overflow = TextOverflow.Ellipsis,
        color = AppTheme.colorScheme.onBackground
      )
      HeightSpacer(value = 4.dp)
      StatusMedia(mediaAttachments) {
        media = mediaAttachments
        targetMediaIndex = it
        openDialog = true
      }
      HeightSpacer(value = 6.dp)
      ConstraintLayout(
        modifier = Modifier
          .padding(end = 24.dp)
          .fillMaxWidth()
      ) {
        val (replyIcon, repeatIcon, favoriteIcon,
          replyCount, repeatCount, favoriteCount) = createRefs()
        actionList.forEach {
          ClickableIcon(
            painter = painterResource(id = it.key),
            tint = Color.Gray,
            modifier = Modifier
              .size(22.dp)
              .constrainAs(
                ref = when (it.key) {
                  R.drawable.chat_3_line-> replyIcon
                  R.drawable.repeat_2_line -> repeatIcon
                  else -> favoriteIcon
                }
              ) {
                start.linkTo(
                  anchor = when (it.key) {
                    R.drawable.chat_3_line -> parent.start
                    R.drawable.repeat_2_line -> replyIcon.end
                    else -> repeatIcon.end
                  },
                  margin = if (it.key == R.drawable.chat_3_line) 0.dp else 100.dp
                )
              }
          )
          if (it.value != 0) {
            Text(
              text = it.value.toString(),
              style = AppTheme.typography.bodyMedium,
              color = Color.Gray,
              modifier = Modifier.constrainAs(
                ref = when (it.key) {
                  R.drawable.chat_3_line -> replyCount
                  R.drawable.repeat_2_line -> repeatCount
                  else -> favoriteCount
                },
              ) {
                start.linkTo(
                  anchor = when (it.key) {
                    R.drawable.chat_3_line -> replyIcon.end
                    R.drawable.repeat_2_line -> repeatIcon.end
                    else -> favoriteIcon.end
                  },
                  margin = 6.dp
                )
              }
            )
          }
        }
      }
    }
  }
  AnimatedVisibility (openDialog) {
    StatusMediaDialog(
      avatar = avatar,
      content = content,
      media = media,
      targetMediaIndex = targetMediaIndex
    ) {
      openDialog = false
    }
  }
}
