package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.MyHtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.launchCustomChromeTab

@Composable
fun StatusContent(
  modifier: Modifier = Modifier,
  reblogStatus: ReblogStatus,
  avatar: String,
  displayName: String,
  username: String,
  instanceName: String,
  createdAt: String,
  content: String,
  application: Status.Application?,
  sensitive: Boolean,
  spoilerText: String,
  mentions: List<Status.Mention>,
  tags: List<Status.Tag>,
  attachments: List<Status.Attachment>,
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int
) {

  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  var openDialog by rememberSaveable { mutableStateOf(false) }
  var targetMediaIndex by remember { mutableIntStateOf(0) }
  var media by remember { mutableStateOf<List<Status.Attachment>>(listOf()) }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    shape = RoundedCornerShape(18.dp),
    color = AppTheme.colors.cardBackground
  ) {
    Column {
      if (reblogStatus.reblog) {
        Column {
          CenterRow(
            Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 8.dp, start = 14.dp, end = 24.dp)) {
            CircleShapeAsyncImage(
              model = reblogStatus.originalAccountAvatar,
              modifier = Modifier.size(24.dp)
            )
            WidthSpacer(value = 4.dp)
            Text(
              buildAnnotatedString {
                withStyle(
                  SpanStyle(
                    color = AppTheme.colors.cardCaption60,
                    fontSize = AppTheme.typography.statusRepost.fontSize,
                  )
                ) {
                  append("由 ")
                }
                withStyle(
                  SpanStyle(
                    color = AppTheme.colors.cardCaption,
                    fontSize = AppTheme.typography.statusRepost.fontSize,
                  )
                ) {
                  append(reblogStatus.originalAccountName)
                }
                withStyle(
                  SpanStyle(
                    color = AppTheme.colors.cardCaption60,
                    fontSize = AppTheme.typography.statusRepost.fontSize,
                  )
                ) {
                  append(" 转发")
                }
              },
              modifier = Modifier.weight(1f)
            )
            Image(
              painter = painterResource(id = R.drawable.reblog),
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          }
          Divider(thickness = 1.dp, color = AppTheme.colors.background)
        }
      }
      Column(modifier = Modifier.padding(12.dp)) {
        CenterRow(modifier = Modifier.fillMaxWidth()) {
          CircleShapeAsyncImage(
            model = avatar,
            modifier = Modifier.size(36.dp)
          )
          WidthSpacer(value = 7.dp)
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = displayName,
              style = AppTheme.typography.statusDisplayName,
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
            Text(
              text = "@$username@$instanceName ${FormatFactory.getTimeDiff(createdAt)}",
              style = AppTheme.typography.statusUsername.copy(
                color = AppTheme.colors.primaryContent.copy(alpha = 0.48f)
              ),
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
          }
          ClickableIcon(
            painter = painterResource(id = R.drawable.more),
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
          )
        }
        if (content.isNotEmpty()) {
          HeightSpacer(value = 4.dp)
          MyHtmlText(
            text = content,
            fontSize = 14.sp,
            maxLines = 11,
            overflow = TextOverflow.Ellipsis,
            color = AppTheme.colors.primaryContent,
          ) { span ->
            launchCustomChromeTab(
              context = context,
              uri = Uri.parse(span),
              toolbarColor = primaryColor.toArgb()
            )
          }
        }
        if (attachments.isNotEmpty()) {
          HeightSpacer(value = 4.dp)
          StatusMedia(
            attachments = attachments,
            onClick = {
              media = attachments
              targetMediaIndex = it
              openDialog = true
            }
          )
        }
        HeightSpacer(value = 6.dp)
        ActionsRow(
          repliesCount = repliesCount,
          reblogsCount = reblogsCount,
          favouritesCount = favouritesCount
        )
      }
    }
  }
  if (openDialog) {
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

@Composable
fun ActionsRow(
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
) {
  CenterRow(
    modifier = Modifier.padding(12.dp)
  ) {
    CenterRow(modifier = Modifier.weight(1f)) {
      ClickableIcon(
        painter = painterResource(id = R.drawable.chat),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
      WidthSpacer(value = 2.dp)
      Text(
        text = repliesCount.toString(),
        style = AppTheme.typography.statusActions
      )
      WidthSpacer(value = 24.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.heart),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
      WidthSpacer(value = 2.dp)
      Text(
        text = favouritesCount.toString(),
        style = AppTheme.typography.statusActions,
      )
      WidthSpacer(value = 24.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.repost),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
      WidthSpacer(value = 2.dp)
      Text(
        text = reblogsCount.toString(),
        color = AppTheme.colors.cardAction,
      )
    }
    CenterRow {
      Surface(
        modifier = Modifier
          .size(height = 16.dp, width = 1.dp)
          .clip(RoundedCornerShape(100.dp)),
        color = AppTheme.colors.cardAction.copy(alpha = 0.12f)
      ) { }
      WidthSpacer(value = 16.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.share),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction
      )
    }
  }
}
