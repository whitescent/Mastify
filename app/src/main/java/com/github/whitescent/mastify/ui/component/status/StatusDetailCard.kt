package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.SensitiveBar
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.htmlText.HtmlText
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import kotlinx.collections.immutable.ImmutableList

@Composable
fun StatusDetailCard(
  status: StatusUiData,
  modifier: Modifier = Modifier,
  inReply: Boolean = false,
  backgroundColor: Color = AppTheme.colors.cardBackground,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  navigateToDetail: () -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit,
) {
  val normalShape by remember { mutableStateOf(RoundedCornerShape(18.dp)) }
  val endShape by remember {
    mutableStateOf(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
  }
  var hideSensitiveContent by rememberSaveable(status.sensitive) {
    mutableStateOf(status.sensitive)
  }

  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  val avatarSizePx = with(LocalDensity.current) { statusAvatarSize.toPx() }
  val contentPaddingPx = with(LocalDensity.current) { statusContentPadding.toPx() }
  val avatarHalfSize = avatarSizePx / 2
  val avatarCenterX = avatarHalfSize + contentPaddingPx
  val replyLineColor = AppTheme.colors.replyLine

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = if (inReply) endShape else normalShape,
    color = backgroundColor
  ) {
    Column(
      modifier = Modifier
        .clickable(
          onClick = navigateToDetail,
          indication = null,
          interactionSource = remember { MutableInteractionSource() }
        )
        .let {
          if (inReply) {
            it.drawWithContent {
              val (startOffsetY, endOffsetY) = 0f to avatarHalfSize
              drawLine(
                color = replyLineColor,
                start = Offset(avatarCenterX, startOffsetY),
                end = Offset(avatarCenterX, endOffsetY),
                cap = StrokeCap.Round,
                strokeWidth = 4f
              )
              drawContent()
            }
          } else it
        }
        .padding(statusContentPadding)
    ) {
      CenterRow(modifier = Modifier.fillMaxWidth()) {
        CircleShapeAsyncImage(
          model = status.avatar,
          modifier = Modifier.size(statusAvatarSize),
          shape = AppTheme.shape.statusAvatarShape,
          onClick = { navigateToProfile(status.actionable.account) }
        )
        WidthSpacer(value = 7.dp)
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = status.displayName,
            fontSize = 17.sp,
            fontWeight = FontWeight(550),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
          )
          Text(
            text = status.fullname,
            fontSize = 14.sp,
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
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
      Crossfade(hideSensitiveContent) {
        when (it) {
          true -> {
            Column {
              HeightSpacer(value = 4.dp)
              SensitiveBar(spoilerText = status.spoilerText) { hideSensitiveContent = false }
            }
          }
          else -> {
            Column {
              if (status.content.isNotEmpty()) {
                HeightSpacer(value = 4.dp)
                HtmlText(
                  text = status.content.trimEnd(),
                  style = TextStyle(fontSize = 16.sp),
                  maxLines = 11,
                  linkClicked = { span ->
                    launchCustomChromeTab(
                      context = context,
                      uri = Uri.parse(span),
                      toolbarColor = primaryColor.toArgb(),
                    )
                  },
                  overflow = TextOverflow.Ellipsis,
                  nonLinkClicked = { navigateToDetail() }
                )
              }
              if (status.attachments.isNotEmpty()) {
                HeightSpacer(value = 4.dp)
                StatusMedia(
                  attachments = status.attachments,
                  onClick = { targetIndex ->
                    navigateToMedia(status.attachments, targetIndex)
                  },
                )
              }
            }
          }
        }
      }
      HeightSpacer(value = 8.dp)
      StatusDetailInfo(
        reblogsCount = status.reblogsCount,
        favouritesCount = status.favouritesCount,
        createdAt = status.createdAt,
        application = status.application
      )
      HeightSpacer(value = 8.dp)
      StatusDetailActionsRow(
        favourited = status.favourited,
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
    horizontalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    ClickableIcon(
      painter = painterResource(id = R.drawable.chat_teardrop),
      modifier = Modifier.size(statusDetailActionsIconSize),
      tint = AppTheme.colors.primaryContent,
    )
    ClickableIcon(
      painter = painterResource(id = R.drawable.heart2),
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
        text = pluralStringResource(id = R.plurals.favs, favouritesCount, favouritesCount),
        color = Color(0xFFF91880),
      )
      WidthSpacer(value = 8.dp)
      Text(
        text = pluralStringResource(id = R.plurals.reblogs, reblogsCount, reblogsCount),
        color = Color(0xFF00BA7C)
      )
    }
  }
}

private val statusContentPadding = 12.dp
private val statusAvatarSize = 40.dp
private val statusDetailActionsIconSize = 24.dp
