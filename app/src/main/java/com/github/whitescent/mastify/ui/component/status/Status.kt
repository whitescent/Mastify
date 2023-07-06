package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.github.whitescent.mastify.network.model.account.Status
import com.github.whitescent.mastify.ui.component.AnimatedCountText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.htmlText.HtmlText
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.getRelativeTimeSpanString
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

@Composable
fun Status(
  modifier: Modifier = Modifier,
  status: Status,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  navigateToDetail: () -> Unit,
) {
  val avatar = status.reblog?.account?.avatar ?: status.account.avatar
  val reblogAvatar = status.account.avatar

  // status author display name
  val displayName = status.reblog?.account?.displayName?.ifEmpty {
    status.reblog.account.username
  } ?:
    status.account.displayName.ifEmpty { status.account.username }

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

  var openDialog by rememberSaveable { mutableStateOf(false) }
  var targetMediaIndex by remember { mutableIntStateOf(0) }
  var media by remember { mutableStateOf<List<Status.Attachment>>(listOf()) }

  val normalShape = RoundedCornerShape(18.dp)
  val replyShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
  val lastReplyShape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)

  var replyStatusHeight by remember { mutableIntStateOf(0) }
  val avatarSizePx = with(LocalDensity.current) { statusAvatarSize.toPx() }
  val contentPaddingPx = with(LocalDensity.current) { statusContentPadding.toPx() }
  val avatarHalfSize = avatarSizePx / 2
  val avatarCenterX = avatarHalfSize + contentPaddingPx
  val replyLineColor = AppTheme.colors.replyLine

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    shape =
      when(status.hasUnloadedReplyStatus) {
        true -> normalShape
        else -> {
          when(status.replyChainType) {
            Status.ReplyChainType.Start -> replyShape
            Status.ReplyChainType.End -> lastReplyShape
            Status.ReplyChainType.Continue -> RectangleShape
            Status.ReplyChainType.Null -> normalShape
          }
        }
      },
    color = AppTheme.colors.cardBackground,
  ) {
    Column {
      when {
        status.hasMultiReplyStatus -> {
          CenterRow {
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
              text = "显示更多回复",
              fontSize = 14.sp,
              fontWeight = FontWeight(600),
              color = Color(0xFF0079D3),
            )
          }
        }
        status.hasUnloadedReplyStatus -> {
          CenterRow(
            modifier = Modifier
              .padding(horizontal = statusContentPadding)
              .let {
                if (status.replyChainType == Status.ReplyChainType.Start)
                  it.padding(top = statusContentPadding)
                else it
              }
          ) {
            Box(
              modifier = Modifier
                .size(statusAvatarSize),
              contentAlignment = Alignment.Center
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                  Box(Modifier.size(3.dp).background(replyLineColor, CircleShape))
                }
              }
            }
            WidthSpacer(value = 7.dp)
            Text(
              text = "展开了一个讨论串",
              fontSize = 14.sp,
              fontWeight = FontWeight(600),
              color = Color(0xFF0079D3),
            )
          }
        }
      }
      Column(
        modifier = Modifier
          .clickable(
            onClick = navigateToDetail,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
          )
          .drawWithContent {
            val itemHeight = this.size.height
            val (startOffsetY, endOffsetY) = when (status.replyChainType) {
              Status.ReplyChainType.Start -> {
                if (!status.hasUnloadedReplyStatus)
                  avatarHalfSize to itemHeight
                else 0f to itemHeight
              }
              Status.ReplyChainType.Continue -> 0f to itemHeight
              Status.ReplyChainType.End -> 0f to avatarHalfSize
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
          },
      ) {
        status.reblog?.let {
          StatusSource(
            reblogAvatar = reblogAvatar,
            reblogDisplayName = reblogDisplayName
          )
        }
        StatusContent(
          avatar = avatar,
          displayName = displayName,
          fullname = fullname,
          createdAt = createdAt,
          content = content,
          sensitive = sensitive,
          spoilerText = spoilerText,
          replyChainType = status.replyChainType,
          attachments = attachments.toImmutableList(),
          mentions = mentions.toImmutableList(),
          repliesCount = repliesCount,
          reblogsCount = reblogsCount,
          favouritesCount = favouritesCount,
          favourited = favourited,
          navigateToDetail = { navigateToDetail() },
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
          onClickMedia = {
            media = attachments
            targetMediaIndex = it
            openDialog = true
          },
          modifier = Modifier.let {
            if (status.replyChainType == Status.ReplyChainType.Start)
              it.onGloballyPositioned { status ->
                replyStatusHeight = status.size.height
              }
            else it
          }
        )
      }
    }
  }
  if (openDialog) {
    StatusMediaDialog(
      avatar = avatar,
      content = content,
      media = media,
      targetMediaIndex = targetMediaIndex,
    ) {
      openDialog = false
    }
  }
}

@Composable
fun StatusSource(reblogAvatar: String, reblogDisplayName: String) {
  Column {
    CenterRow(
      Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 8.dp, start = 14.dp, end = 24.dp),
    ) {
      CircleShapeAsyncImage(
        model = reblogAvatar,
        modifier = Modifier.size(24.dp),
      )
      WidthSpacer(value = 4.dp)
      Text(
        buildAnnotatedString {
          withStyle(
            SpanStyle(
              color = AppTheme.colors.cardCaption,
              fontSize = AppTheme.typography.statusRepost.fontSize,
            ),
          ) {
            append(reblogDisplayName)
          }
          withStyle(
            SpanStyle(
              color = AppTheme.colors.cardCaption60,
              fontSize = AppTheme.typography.statusRepost.fontSize,
            ),
          ) {
            append(" "+stringResource(id = R.string.post_boosted_format_suffix))
          }
        },
        modifier = Modifier.weight(1f),
      )
      Image(
        painter = painterResource(id = R.drawable.reblog),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
    Divider(thickness = 1.dp, color = AppTheme.colors.background)
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
  replyChainType: Status.ReplyChainType,
  attachments: ImmutableList<Status.Attachment>,
  mentions: ImmutableList<Status.Mention>,
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
  favourited: Boolean,
  navigateToDetail: () -> Unit,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  onClickMedia: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  Box(modifier = modifier) {
    when (replyChainType) {
      Status.ReplyChainType.Continue, Status.ReplyChainType.Start -> {
        Row(modifier = Modifier.padding(statusContentPadding)) {
          CircleShapeAsyncImage(
            model = avatar,
            modifier = Modifier.size(statusAvatarSize),
          )
          WidthSpacer(value = 7.dp)
          Column(modifier = Modifier.align(Alignment.Top)) {
            CenterRow {
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
              CenterRow {
                Text(
                  text = getRelativeTimeSpanString(
                    context,
                    createdAt.toInstant().toEpochMilliseconds(),
                    Clock.System.now().toEpochMilliseconds()
                  ),
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
                  fontSize = 14.sp,
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
            }
            if (attachments.isNotEmpty()) {
              HeightSpacer(value = 4.dp)
              StatusMedia(
                sensitive = sensitive,
                spoilerText = spoilerText,
                attachments = attachments,
                onClick = onClickMedia,
              )
            }
            HeightSpacer(value = 6.dp)
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
      else -> {
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
            CenterRow {
              Text(
                text = getRelativeTimeSpanString(
                  context,
                  createdAt.toInstant().toEpochMilliseconds(),
                  Clock.System.now().toEpochMilliseconds()
                ),
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
                fontSize = 14.sp,
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
          }
          if (attachments.isNotEmpty()) {
            HeightSpacer(value = 4.dp)
            StatusMedia(
              sensitive = sensitive,
              spoilerText = spoilerText,
              attachments = attachments,
              onClick = onClickMedia,
            )
          }
          HeightSpacer(value = 6.dp)
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

  CenterRow(
    modifier = modifier,
  ) {
    CenterRow(modifier = Modifier.weight(1f)) {
      ClickableIcon(
        painter = painterResource(id = R.drawable.chat),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction,
      )
      WidthSpacer(value = 2.dp)
      Text(
        text = repliesCount.toString(),
        style = AppTheme.typography.statusActions,
      )
      WidthSpacer(value = 24.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.heart),
        modifier = Modifier
          .size(20.dp),
        tint = animatedFavIconColor,
      ) {
        favState = !favState
        if (favState) {
          animatedFavCount += 1
          favouriteStatus()
        } else {
          animatedFavCount -=1
          unfavouriteStatus()
        }
      }
      WidthSpacer(value = 2.dp)
      AnimatedCountText(
        count = animatedFavCount,
        style = AppTheme.typography.statusActions,
      )
      WidthSpacer(value = 24.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.repost),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction,
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
        color = AppTheme.colors.cardAction.copy(alpha = 0.12f),
      ) { }
      WidthSpacer(value = 16.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.share),
        modifier = Modifier
          .size(20.dp),
        tint = AppTheme.colors.cardAction,
      )
    }
  }
}

private val statusContentPadding = 12.dp
private val statusAvatarSize = 36.dp
