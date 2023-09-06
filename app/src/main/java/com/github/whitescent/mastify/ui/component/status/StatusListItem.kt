package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AnimatedCountText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.SensitiveBar
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.getRelativeTimeSpanString
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.github.whitescent.mastify.viewModel.StatusMenuAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

@Composable
fun StatusListItem(
  status: StatusUiData,
  replyChainType: ReplyChainType,
  hasUnloadedParent: Boolean,
  modifier: Modifier = Modifier,
  menuAction: (StatusMenuAction) -> Unit,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  reblogStatus: () -> Unit,
  unreblogStatus: () -> Unit,
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
            reblogDisplayName = status.reblogDisplayName
          ) { navigateToProfile(status.account) }
        }
        StatusContent(
          avatar = status.avatar,
          displayName = status.displayName,
          fullname = status.fullname,
          createdAt = status.createdAt,
          content = status.content,
          sensitive = status.sensitive,
          spoilerText = status.spoilerText,
          attachments = status.attachments,
          repliesCount = status.repliesCount,
          reblogsCount = status.reblogsCount,
          menuAction = menuAction,
          favouritesCount = status.favouritesCount,
          favourited = status.favourited,
          reblogged = status.reblogged,
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
          reblogStatus = reblogStatus,
          unreblogStatus = unreblogStatus,
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
      shape = AppTheme.shape.avatarShape,
      onClick = { navigateToProfile() }
    )
    WidthSpacer(value = 4.dp)
    HtmlText(
      text = "$reblogDisplayName " + stringResource(id = R.string.post_boosted_format_suffix),
      style = TextStyle(
        color = AppTheme.colors.cardCaption,
        fontSize = AppTheme.typography.statusRepost.fontSize,
      )
    )
    WidthSpacer(value = 4.dp)
    Image(
      painter = painterResource(id = R.drawable.reblog),
      contentDescription = null,
      modifier = Modifier.size(16.dp),
    )
  }
}

@Composable
private fun StatusContent(
  avatar: String,
  displayName: String,
  fullname: String,
  createdAt: String,
  content: String,
  sensitive: Boolean,
  spoilerText: String,
  attachments: ImmutableList<Attachment>,
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
  favourited: Boolean,
  reblogged: Boolean,
  menuAction: (StatusMenuAction) -> Unit,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  reblogStatus: () -> Unit,
  unreblogStatus: () -> Unit,
  onClickMedia: (Int) -> Unit,
  navigateToProfile: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  var hideSensitiveContent by rememberSaveable(sensitive, spoilerText) {
    mutableStateOf(sensitive && spoilerText.isNotEmpty())
  }
  var openMenu by remember { mutableStateOf(false) }
  var pressOffset by remember { mutableStateOf(IntOffset.Zero) }

  Box(modifier = modifier) {
    Row(modifier = Modifier.padding(statusContentPadding)) {
      CircleShapeAsyncImage(
        model = avatar,
        modifier = Modifier.size(statusAvatarSize),
        shape = AppTheme.shape.avatarShape,
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
              maxLines = 1
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
          Column {
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
                modifier = Modifier
                  .size(18.dp)
                  .onSizeChanged {
                    pressOffset = IntOffset(x = -it.width, y = it.height)
                  },
                onClick = { openMenu = true }
              )
            }
            StatusDropdownMenu(
              expanded = openMenu,
              enableCopyText = content.isNotEmpty(),
              fullname = fullname,
              offset = pressOffset,
              onDismissRequest = { openMenu = false },
            ) {
              menuAction(it)
              openMenu = false
            }
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
                    fontSize = 15.sp,
                    maxLines = 11,
                    onLinkClick = { span ->
                      launchCustomChromeTab(
                        context = context,
                        uri = Uri.parse(span),
                        toolbarColor = primaryColor.toArgb(),
                      )
                    },
                    overflow = TextOverflow.Ellipsis,
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
        HeightSpacer(value = 6.dp)
        StatusActionsRow(
          repliesCount = repliesCount,
          reblogsCount = reblogsCount,
          favouritesCount = favouritesCount,
          favourited = favourited,
          reblogged = reblogged,
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
          reblogStatus = reblogStatus,
          unreblogStatus = unreblogStatus
        )
      }
    }
  }
}

@Composable
private fun StatusActionsRow(
  repliesCount: Int,
  reblogsCount: Int,
  favouritesCount: Int,
  favourited: Boolean,
  reblogged: Boolean,
  favouriteStatus: () -> Unit,
  unfavouriteStatus: () -> Unit,
  reblogStatus: () -> Unit,
  unreblogStatus: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()

  val favouriteColor = AppTheme.colors.cardLike
  val unfavouriteColor = AppTheme.colors.cardAction

  var favState by remember(favourited) { mutableStateOf(favourited) }
  var animatedFavCount by remember(favouritesCount) { mutableIntStateOf(favouritesCount) }
  val animatedFavIconColor by animateColorAsState(
    targetValue = if (favState) favouriteColor else unfavouriteColor,
  )

  val reblogColor = Color(0xFF18BE64)
  val unreblogColor = AppTheme.colors.cardAction

  val reblogScaleAnimatable = remember { Animatable(1f) }
  val reblogRotateAnimatable = remember { Animatable(0f) }
  var reblogState by remember(reblogged) { mutableStateOf(reblogged) }
  var animatedReblogCount by remember(reblogsCount) { mutableIntStateOf(reblogsCount) }
  val animatedReblogIconColor by animateColorAsState(
    targetValue = if (reblogState) reblogColor else unreblogColor,
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
          painter = painterResource(id = if (favState) R.drawable.heart_fill else R.drawable.heart),
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
          painter = painterResource(if (reblogState) R.drawable.share_fill else R.drawable.share_fat),
          modifier = Modifier
            .size(statusActionsIconSize)
            .scale(reblogScaleAnimatable.value)
            .rotate(reblogRotateAnimatable.value),
          tint = animatedReblogIconColor,
        ) {
          reblogState = !reblogState
          if (reblogState) {
            animatedReblogCount += 1
            reblogStatus()
          } else {
            animatedReblogCount -= 1
            unreblogStatus()
          }
          scope.launch {
            reblogRotateAnimatable.animateTo(
              targetValue = if (reblogRotateAnimatable.value == 0f) 360f else 0f,
              animationSpec = tween(durationMillis = 300)
            )
            reblogScaleAnimatable.animateTo(1.4f, animationSpec = tween(durationMillis = 150))
            reblogScaleAnimatable.animateTo(1f, animationSpec = tween(durationMillis = 150))
          }
        }
        WidthSpacer(value = 2.dp)
        AnimatedCountText(
          count = animatedReblogCount,
          style = TextStyle(color = AppTheme.colors.cardAction),
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
