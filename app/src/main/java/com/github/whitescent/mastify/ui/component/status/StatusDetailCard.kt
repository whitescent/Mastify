package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.htmlText.HtmlText
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.launchCustomChromeTab

@Composable
fun StatusDetailCard(
  status: Status,
  modifier: Modifier = Modifier,
  backgroundColor: Color = AppTheme.colors.cardBackground,
  contentTextStyle: TextStyle = LocalTextStyle.current,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  val avatar = status.reblog?.account?.avatar ?: status.account.avatar
  // status author display name
  val displayName = status.reblog?.account?.displayName?.ifEmpty {
    status.reblog.account.username
  } ?: status.account.displayName.ifEmpty { status.account.username }

  // The display name of the person who forwarded this status
  val reblogDisplayName = status.account.displayName.ifEmpty { status.account.username }

  val fullname = status.reblog?.account?.fullName ?: status.account.fullName
  val createdAt = status.reblog?.createdAt ?: status.createdAt
  val content = status.reblog?.content ?: status.content
  val application = status.reblog?.application ?: status.application
  val sensitive = status.reblog?.sensitive ?: status.sensitive
  val spoilerText = status.reblog?.spoilerText ?: status.spoilerText
  val mentions = status.reblog?.mentions ?: status.mentions
  val tags = status.reblog?.tags ?: status.tags
  val attachments = status.reblog?.attachments ?: status.attachments
  val repliesCount = status.reblog?.repliesCount ?: status.repliesCount
  val reblogsCount = status.reblog?.reblogsCount ?: status.reblogsCount
  val favouritesCount = status.reblog?.favouritesCount ?: status.favouritesCount
  val favourited = status.reblog?.favourited ?: status.favourited

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(12.dp),
    shape = RoundedCornerShape(18.dp),
    color = backgroundColor,
  ) {
    val context = LocalContext.current
    val primaryColor = AppTheme.colors.primaryContent
    Column(
      modifier = Modifier.padding(statusContentPadding)
    ) {
      CenterRow(modifier = Modifier.fillMaxWidth()) {
        CircleShapeAsyncImage(
          model = avatar,
          modifier = Modifier.size(statusAvatarSize),
        )
        WidthSpacer(value = 7.dp)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = displayName,
            style = AppTheme.typography.statusDisplayName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
          )
          Text(
            text = fullname,
            style = AppTheme.typography.statusUsername.copy(
              color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
          )
        }
        WidthSpacer(value = 4.dp)
        ClickableIcon(
          painter = painterResource(id = R.drawable.more),
          tint = AppTheme.colors.cardMenu,
          modifier = Modifier.size(18.dp),
        )
      }
      if (content.isNotEmpty()) {
        var mutableSensitive by rememberSaveable(sensitive) { mutableStateOf(sensitive) }
        HeightSpacer(value = 4.dp)
        if (mutableSensitive) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF3f3131),
          ) {
            CenterRow(
              modifier = Modifier
                .clickable {
                  mutableSensitive = !mutableSensitive
                }
                .padding(8.dp),
            ) {
              Icon(
                painter = painterResource(id = R.drawable.warning_circle),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
              )
              WidthSpacer(value = 4.dp)
              Text(
                text = spoilerText.ifEmpty { stringResource(id = R.string.sensitive_content) },
                color = Color.White,
              )
            }
          }
        }
        AnimatedVisibility(visible = !mutableSensitive) {
          HtmlText(
            text = content.trimEnd(),
            style = contentTextStyle,
            linkClicked = { span ->
              launchCustomChromeTab(
                context = context,
                uri = Uri.parse(span),
                toolbarColor = primaryColor.toArgb(),
              )
            },
          )
        }
      }
      if (attachments.isNotEmpty()) {
        HeightSpacer(value = 4.dp)
        StatusMedia(
          sensitive = sensitive,
          spoilerText = spoilerText,
          attachments = attachments,
          onClick = { navigateToMedia(attachments, it) },
        )
      }
      HeightSpacer(value = 8.dp)
      StatusDetailInfo(
        reblogsCount = reblogsCount,
        favouritesCount = favouritesCount,
        createdAt = createdAt,
        application = application
      )
      HeightSpacer(value = 8.dp)
      StatusDetailActionsRow(
        favourited = favourited,
        favouriteStatus = favouriteStatus,
        unfavouriteStatus = unfavouriteStatus,
      )
    }
  }
}

@Composable
fun StatusDetailActionsRow(
  favourited: Boolean,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  modifier: Modifier = Modifier
) {
  val favouritedColor = AppTheme.colors.cardLike
  val unfavouritedColor = AppTheme.colors.primaryContent

  var favState by remember(favourited) { mutableStateOf(favourited) }
  val animatedFavIconColor by animateColorAsState(
    targetValue = if (favState) favouritedColor else unfavouritedColor,
  )

  CenterRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    ClickableIcon(
      painter = painterResource(id = R.drawable.chat),
      modifier = Modifier.size(statusDetailActionsIconSize),
      tint = AppTheme.colors.primaryContent,
    )
    ClickableIcon(
      painter = painterResource(id = R.drawable.heart),
      modifier = Modifier.size(statusDetailActionsIconSize),
      tint = animatedFavIconColor,
    ) {
      favState = !favState
      if (favState) favouriteStatus()
      else unfavouriteStatus()
    }
    ClickableIcon(
      painter = painterResource(id = R.drawable.share_fat),
      modifier = Modifier.size(statusDetailActionsIconSize),
      tint = AppTheme.colors.primaryContent,
    )
    ClickableIcon(
      painter = painterResource(id = R.drawable.bookmark_simple),
      modifier = Modifier.size(statusDetailActionsIconSize),
      tint = AppTheme.colors.primaryContent,
    )
    ClickableIcon(
      painter = painterResource(id = R.drawable.share_network),
      modifier = Modifier.size(statusDetailActionsIconSize),
      tint = AppTheme.colors.primaryContent,
    )
  }
}

@Composable
fun StatusDetailInfo(
  reblogsCount: Int,
  favouritesCount: Int,
  createdAt: String,
  application: Application?,
) {
  Column {
    CenterRow(Modifier.fillMaxWidth()) {
      CenterRow(Modifier.weight(1f)) {
        Icon(
          painter = painterResource(id = R.drawable.globe),
          contentDescription = null,
          tint = Color(0xFF999999),
          modifier = Modifier.size(20.dp)
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = "${FormatFactory.getLocalizedDateTime(createdAt)} ${FormatFactory.getTime(createdAt)}",
          color = Color(0xFF999999),
          fontSize = 14.sp,
        )
      }
      application?.let {
        if (it.name.isNotEmpty()) {
          Text(
            text = "来自 ${application.name}",
            color = AppTheme.colors.hintText
          )
        }
      }
    }
    HeightSpacer(value = 8.dp)
    CenterRow {
      Text(
        text = "$favouritesCount 喜欢",
        color = Color(0xFFF91880)
      )
      WidthSpacer(value = 8.dp)
      Text(
        text = "$reblogsCount 转嘟",
        color = Color(0xFF00BA7C)
      )
    }
  }
}

private val statusContentPadding = 12.dp
private val statusAvatarSize = 36.dp
private val statusDetailActionsIconSize = 24.dp
