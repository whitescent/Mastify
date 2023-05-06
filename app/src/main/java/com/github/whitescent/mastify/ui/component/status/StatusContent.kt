package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.github.whitescent.R
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.network.model.response.account.Application
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.network.model.response.account.Mention
import com.github.whitescent.mastify.network.model.response.account.Tag
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.MyHtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.launchCustomChromeTab

@Composable
fun StatusContent(
  avatar: String,
  displayName: String,
  username: String,
  instanceName: String,
  createdAt: String,
  content: String,
  application: Application?,
  sensitive: Boolean,
  spoilerText: String,
  mentions: List<Mention>,
  tags: List<Tag>,
  mediaAttachments: List<MediaAttachments>,
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int
) {
  
  val actionList = mapOf(
    R.drawable.chat_circle to repliesCount,
    R.drawable.repeat_fill to reblogsCount,
    R.drawable.heart to favouritesCount
  )

  val context = LocalContext.current
  val primaryColor = AppTheme.colorScheme.primary
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
            painter = painterResource(id = R.drawable.more),
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      if (content.isNotEmpty()) {
        HeightSpacer(value = 4.dp)
        MyHtmlText(
          text = content,
          fontSize = 14.sp,
          maxLines = 11,
          overflow = TextOverflow.Ellipsis,
          color = AppTheme.colorScheme.onBackground
        ) { span ->
          launchCustomChromeTab(
            context = context,
            uri = Uri.parse(span),
            toolbarColor = primaryColor.toArgb()
          )
        }
      }
      if (mediaAttachments.isNotEmpty()) {
        HeightSpacer(value = 4.dp)
        StatusMedia(
          mediaAttachments = mediaAttachments,
          onClick = {
            media = mediaAttachments
            targetMediaIndex = it
            openDialog = true
          }
        )
      }
      application?.let {
        HeightSpacer(value = 4.dp)
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.CenterEnd
        ) {
          Box(
            modifier = Modifier.background(Color(0xff0079D3), RoundedCornerShape(12.dp))
          ) {
            Text(
              text = it.name,
              style = AppTheme.typography.bodyMedium,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              color = Color.White
            )
          }
        }
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
              .size(20.dp)
              .constrainAs(
                ref = when (it.key) {
                  R.drawable.chat_circle -> replyIcon
                  R.drawable.repeat_fill -> repeatIcon
                  else -> favoriteIcon
                }
              ) {
                start.linkTo(
                  anchor = when (it.key) {
                    R.drawable.chat_circle -> parent.start
                    R.drawable.repeat_fill -> replyIcon.end
                    else -> repeatIcon.end
                  },
                  margin = if (it.key == R.drawable.chat_circle) 0.dp else 100.dp
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
                  R.drawable.chat_circle -> replyCount
                  R.drawable.repeat_fill -> repeatCount
                  else -> favoriteCount
                },
              ) {
                start.linkTo(
                  anchor = when (it.key) {
                    R.drawable.chat_circle -> replyIcon.end
                    R.drawable.repeat_fill -> repeatIcon.end
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
  AnimatedVisibility (
    visible = openDialog,
    exit = fadeOut(tween(10))
  ) {
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
