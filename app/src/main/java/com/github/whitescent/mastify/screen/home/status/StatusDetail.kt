package com.github.whitescent.mastify.screen.home.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.htmlText.HtmlText
import com.github.whitescent.mastify.ui.component.status.StatusMedia
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.StatusTransitions
import com.github.whitescent.mastify.utils.FormatFactory.getLocalizedDateTime
import com.github.whitescent.mastify.utils.FormatFactory.getTime
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@AppNavGraph
@Destination(style = StatusTransitions::class)
@Composable
fun StatusDetail(
  status: Status,
  navigator: DestinationsNavigator
) {
  val avatar = status.reblog?.account?.avatar ?: status.account.avatar
  val reblogAvatar = status.account.avatar

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

  val context = LocalContext.current

  Column(Modifier.fillMaxSize()) {
    Spacer(Modifier.statusBarsPadding())
    CenterRow(Modifier.padding(12.dp)) {
      IconButton(onClick = { navigator.popBackStack() }) {
        Icon(
          painter = painterResource(id = R.drawable.arrow_left),
          contentDescription = null,
          modifier = Modifier.size(28.dp)
        )
      }
      WidthSpacer(value = 8.dp)
      Text(
        text = "主页",
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium
      )
    }
    Divider()
    LazyColumn(Modifier.fillMaxSize()) {
      item {
        Column(Modifier.padding(16.dp)) {
          CenterRow(modifier = Modifier.fillMaxWidth()) {
            CircleShapeAsyncImage(
              model = avatar,
              modifier = Modifier.size(48.dp),
            )
            WidthSpacer(value = 7.dp)
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = displayName,
                fontSize = 18.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
              )
              Text(
                text = fullname,
                color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
              )
            }
            WidthSpacer(value = 4.dp)
            ClickableIcon(
              painter = painterResource(id = R.drawable.more),
              tint = AppTheme.colors.cardMenu,
              modifier = Modifier.size(24.dp),
            )
          }
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
              style = TextStyle(fontSize = 20.sp),
              linkClicked = { span ->
                // launchCustomChromeTab(
                //   context = context,
                //   uri = Uri.parse(span),
                //   toolbarColor = primaryColor.toArgb(),
                // )
              },
            )
          }
          if (attachments.isNotEmpty()) {
            HeightSpacer(value = 4.dp)
            StatusMedia(
              sensitive = sensitive,
              spoilerText = spoilerText,
              attachments = attachments,
              onClick = {
                navigator.navigate(
                  StatusMediaScreenDestination(attachments.toTypedArray(), it)
                )
              }
            )
          }
          HeightSpacer(value = 8.dp)
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
                text = "${getLocalizedDateTime(createdAt)} ${getTime(createdAt)}",
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
        }
      }
    }
  }
}
