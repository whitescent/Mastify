package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.mapper.emoji.toShortCode
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AnimatedCountText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.SensitiveBar
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.annotateInlineEmojis
import com.github.whitescent.mastify.ui.component.inlineTextContentWithEmoji
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.getRelativeTimeSpanString
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

@Composable
fun StatusListItem(
  status: StatusUiData,
  replyChainType: ReplyChainType,
  hasUnloadedParent: Boolean,
  modifier: Modifier = Modifier,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  navigateToDetail: () -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit,
) {
  val normalShape = remember { RoundedCornerShape(18.dp) }
  val startShape = remember { RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp) }
  val endShape = remember { RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp) }

  val avatarSizePx = with(LocalDensity.current) { statusAvatarSize.toPx() }
  val contentPaddingPx = with(LocalDensity.current) { statusContentPadding.toPx() }
  val avatarHalfSize = avatarSizePx / 2
  val avatarCenterX = avatarHalfSize + contentPaddingPx
  val replyLineColor = AppTheme.colors.replyLine

  Surface(
    modifier = modifier,
    shape = when (hasUnloadedParent) {
      true -> {
        when (replyChainType) {
          Start, Continue -> startShape
          else -> normalShape
        }
      }
      else -> {
        when (replyChainType) {
          Start -> startShape
          End -> endShape
          Continue -> RectangleShape
          Null -> normalShape
        }
      }
    },
    color = Color.Transparent
  ) {
    Column {
      if (hasUnloadedParent && (status.reblog == null)) {
        CenterRow(
          modifier = Modifier.padding(top = statusContentPadding)
        ) {
          Box(
            modifier = Modifier
              .padding(horizontal = statusContentPadding)
              .size(statusAvatarSize),
            contentAlignment = Alignment.Center
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              repeat(3) {
                Box(Modifier.size(3.dp).background(replyLineColor, CircleShape))
              }
            }
          }
          Text(
            text = stringResource(id = R.string.started_a_discussion_thread),
            fontSize = 14.sp,
            fontWeight = FontWeight(600),
            color = AppTheme.colors.hintText,
          )
        }
      }
      Column(
        modifier = Modifier
          .clickable(
            onClick = navigateToDetail,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
          )
          .let {
            if (status.reblog == null) {
              it.drawWithContent {
                val itemHeight = this.size.height
                val (startOffsetY, endOffsetY) = when (replyChainType) {
                  Start -> {
                    if (!hasUnloadedParent) avatarHalfSize to itemHeight
                    else 0f to itemHeight
                  }
                  Continue -> 0f to itemHeight
                  End -> 0f to avatarHalfSize
                  else -> 0f to 0f
                }
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
      ) {
        status.reblog?.let {
          StatusSource(
            reblogAvatar = status.rebloggedAvatar,
            reblogDisplayName = status.reblogDisplayName,
            reblogAccountEmojis = status.account.emojis.toImmutableList(),
            navigateToProfile = { navigateToProfile(status.account) }
          )
        }
        StatusContent(
          avatar = status.avatar,
          displayName = status.displayName,
          fullname = status.fullname,
          createdAt = status.createdAt,
          content = status.content,
          sensitive = status.sensitive,
          spoilerText = status.spoilerText,
          emojis = status.emojis,
          accountEmojis = status.accountEmojis,
          attachments = status.attachments,
          repliesCount = status.repliesCount,
          reblogsCount = status.reblogsCount,
          favouritesCount = status.favouritesCount,
          favourited = status.favourited,
          navigateToDetail = { navigateToDetail() },
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
          onClickMedia = {
            navigateToMedia(status.attachments, it)
          },
          navigateToProfile = { navigateToProfile(status.actionable.account) }
        )
      }
    }
  }
}

@Composable
fun StatusSource(
  reblogAvatar: String,
  reblogDisplayName: String,
  reblogAccountEmojis: ImmutableList<Emoji>,
  navigateToProfile: () -> Unit
) {
  CenterRow(
    Modifier
      .fillMaxWidth()
      .padding(start = 24.dp, top = 8.dp),
  ) {
    CircleShapeAsyncImage(
      model = reblogAvatar,
      modifier = Modifier.size(24.dp),
      shape = AppTheme.shape.statusAvatarShape,
      onClick = { navigateToProfile() }
    )
    WidthSpacer(value = 4.dp)
    Text(
      text = buildAnnotatedString {
        withStyle(
          SpanStyle(
            color = AppTheme.colors.cardCaption,
            fontSize = AppTheme.typography.statusRepost.fontSize,
          ),
        ) {
          annotateInlineEmojis(reblogDisplayName, reblogAccountEmojis.toShortCode(), this)
        }
        withStyle(
          SpanStyle(
            color = AppTheme.colors.cardCaption60,
            fontSize = AppTheme.typography.statusRepost.fontSize,
          ),
        ) {
          append(" " + stringResource(id = R.string.post_boosted_format_suffix))
        }
      },
      inlineContent = inlineTextContentWithEmoji(reblogAccountEmojis),
    )
    WidthSpacer(value = 6.dp)
    Image(
      painter = painterResource(id = R.drawable.reblog),
      contentDescription = null,
      modifier = Modifier.size(16.dp),
    )
  }
}

@Composable
fun StatusContent(
  avatar: String,
  displayName: String,
  fullname: String,
  createdAt: String,
  content: String,
  sensitive: Boolean,
  spoilerText: String,
  emojis: ImmutableList<Emoji>,
  accountEmojis: ImmutableList<Emoji>,
  attachments: ImmutableList<Attachment>,
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
  favourited: Boolean,
  navigateToDetail: () -> Unit,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  onClickMedia: (Int) -> Unit,
  navigateToProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  var hideSensitiveContent by rememberSaveable(sensitive) { mutableStateOf(sensitive) }

  Box(modifier = modifier) {
    Row(modifier = Modifier.padding(statusContentPadding)) {
      CircleShapeAsyncImage(
        model = avatar,
        modifier = Modifier.size(statusAvatarSize),
        shape = AppTheme.shape.statusAvatarShape,
        onClick = { navigateToProfile() }
      )
      WidthSpacer(value = 7.dp)
      Column(modifier = Modifier.align(Alignment.Top)) {
        CenterRow {
          Column(modifier = Modifier.weight(1f)) {
            HtmlText(
              text = displayName,
              style = AppTheme.typography.statusDisplayName,
              overflow = TextOverflow.Ellipsis,
              maxLines = 1,
              inlineContent = inlineTextContentWithEmoji(accountEmojis),
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
          CenterRow {
            Text(
              text = remember(createdAt) {
                getRelativeTimeSpanString(
                  context,
                  createdAt.toInstant().toEpochMilliseconds(),
                  Clock.System.now().toEpochMilliseconds()
                )
              },
              style = AppTheme.typography.statusUsername.copy(
                color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
              ),
              overflow = TextOverflow.Ellipsis,
              maxLines = 1,
            )
            WidthSpacer(value = 4.dp)
            ClickableIcon(
              painter = painterResource(id = R.drawable.more),
              tint = AppTheme.colors.cardMenu,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Crossfade(hideSensitiveContent) {
          when (it) {
            true -> {
              Column {
                HeightSpacer(value = 4.dp)
                SensitiveBar(spoilerText = spoilerText) { hideSensitiveContent = false }
              }
            }
            else -> {
              Column {
                if (content.isNotEmpty()) {
                  HeightSpacer(value = 4.dp)
                  HtmlText(
                    text = content,
                    fontSize = 14.sp,
                    maxLines = 11,
                    onClickLink = { span ->
                      launchCustomChromeTab(
                        context = context,
                        uri = Uri.parse(span),
                        toolbarColor = primaryColor.toArgb(),
                      )
                    },
                    overflow = TextOverflow.Ellipsis,
                    onClick = { navigateToDetail() },
                    inlineContent = inlineTextContentWithEmoji(emojis, 14.sp),
                  )
                }
                if (attachments.isNotEmpty()) {
                  HeightSpacer(value = 4.dp)
                  StatusMedia(
                    attachments = attachments,
                    onClick = onClickMedia,
                  )
                }
              }
            }
          }
        }
        HeightSpacer(value = 8.dp)
        StatusActionsRow(
          repliesCount = repliesCount,
          reblogsCount = reblogsCount,
          favouritesCount = favouritesCount,
          favourited = favourited,
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
        )
      }
    }
  }
}

@Composable
fun StatusActionsRow(
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
  favourited: Boolean,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  modifier: Modifier = Modifier
) {
  val favouritedColor = AppTheme.colors.cardLike
  val unfavouritedColor = AppTheme.colors.cardAction

  var favState by remember(favourited) { mutableStateOf(favourited) }
  var animatedFavCount by remember(favouritesCount) { mutableIntStateOf(favouritesCount) }
  val animatedFavIconColor by animateColorAsState(
    targetValue = if (favState) favouritedColor else unfavouritedColor,
  )

  CenterRow(modifier = modifier) {
    CenterRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
      CenterRow {
        ClickableIcon(
          painter = painterResource(id = R.drawable.chat),
          modifier = Modifier.size(statusActionsIconSize),
          tint = AppTheme.colors.cardAction,
        )
        WidthSpacer(value = 2.dp)
        Text(
          text = repliesCount.toString(),
          style = AppTheme.typography.statusActions,
        )
      }
      CenterRow {
        ClickableIcon(
          painter = painterResource(id = R.drawable.heart),
          modifier = Modifier.size(statusActionsIconSize),
          tint = animatedFavIconColor,
        ) {
          favState = !favState
          if (favState) {
            animatedFavCount += 1
            favouriteStatus()
          } else {
            animatedFavCount -= 1
            unfavouriteStatus()
          }
        }
        WidthSpacer(value = 2.dp)
        AnimatedCountText(
          count = animatedFavCount,
          style = AppTheme.typography.statusActions,
        )
      }
      CenterRow {
        ClickableIcon(
          painter = painterResource(id = R.drawable.repost),
          modifier = Modifier.size(statusActionsIconSize),
          tint = AppTheme.colors.cardAction,
        )
        WidthSpacer(value = 2.dp)
        Text(
          text = reblogsCount.toString(),
          color = AppTheme.colors.cardAction,
        )
      }
    }
    CenterRow {
      Surface(
        modifier = Modifier
          .size(height = 16.dp, width = 1.dp)
          .clip(RoundedCornerShape(100.dp)),
        color = AppTheme.colors.cardAction.copy(alpha = 0.12f),
      ) { }
      WidthSpacer(value = 16.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.share),
        modifier = Modifier.size(statusActionsIconSize),
        tint = AppTheme.colors.cardAction,
      )
    }
  }
}
private val statusContentPadding = 12.dp
private val statusAvatarSize = 40.dp
private val statusActionsIconSize = 20.dp
