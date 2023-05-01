package com.github.whitescent.mastify.screen.profile.pager.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.screen.profile.MyHtmlText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.getInstanceName

@Composable
fun StatusList(
  statuses: List<Status>,
  instanceName: String
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize()
  ) {
    itemsIndexed(statuses) { _, status ->
      if (status.content.isNotEmpty() || status.mediaAttachments.isNotEmpty() ||
        status.reblog != null
      ) {
        StatusListItem(
          status = status,
          instanceName = instanceName
        )
      }
      Divider(modifier = Modifier.fillMaxWidth(), thickness = (0.6).dp)
    }
  }
}

@Composable
fun StatusListItem(
  status: Status,
  instanceName: String
) {
  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    status.reblog?.let {
      CenterRow(
        modifier = Modifier.padding(start = 24.dp, top = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Repeat,
          contentDescription = null,
          tint = Color.Gray
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = "${status.account.username} 转了这篇嘟文",
          style = AppTheme.typography.titleSmall,
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
      }
    }
    when (status.reblog) {
      null -> {
        StatusContent(
          avatar = status.account.avatar,
          username = status.account.username,
          instanceName = instanceName,
          createdAt = status.createdAt,
          content = status.content,
          mediaAttachments = status.mediaAttachments,
          repliesCount = status.repliesCount,
          reblogsCount = status.reblogsCount,
          favouritesCount = status.favouritesCount
        )
      }
      else -> {
        StatusContent(
          avatar = status.reblog.account.avatar,
          username = status.reblog.account.username,
          instanceName = getInstanceName(status.account.url)!!,
          createdAt = status.reblog.createdAt,
          content = status.reblog.content,
          mediaAttachments = status.reblog.mediaAttachments,
          repliesCount = status.reblog.repliesCount,
          reblogsCount = status.reblog.reblogsCount,
          favouritesCount = status.reblog.favouritesCount
        )
      }
    }
  }
}

@Composable
fun StatusContent(
  avatar: String,
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
    Icons.Rounded.Reply to repliesCount,
    Icons.Rounded.Repeat to reblogsCount,
    Icons.Rounded.Favorite to favouritesCount
  )
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
      CenterRow(Modifier.fillMaxWidth()) {
        Text(
          text = username,
          style = AppTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = "@$username@$instanceName",
          style = AppTheme.typography.titleSmall,
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Box(
          Modifier
            .padding(horizontal = 8.dp)
            .size(2.dp)
            .background(Color.Gray, CircleShape)
        )
        Text(
          text = FormatFactory.getTimeDiff(createdAt),
          style = AppTheme.typography.titleSmall,
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.CenterEnd
        ) {
          ClickableIcon(
            imageVector = Icons.Rounded.MoreVert,
            tint = Color.Gray
          )
        }
      }
      MyHtmlText(
        text = content,
        style = AppTheme.typography.titleMedium
      )
      mediaAttachments.forEach {
        if (it.type == "image") {
          Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(bottom = 6.dp)
          ) {
            AsyncImage(
              model = it.url,
              contentDescription = null,
              modifier = Modifier.fillMaxSize()
            )
          }
        }
      }
      HeightSpacer(value = 3.dp)
      ConstraintLayout(
        modifier = Modifier
          .padding(end = 24.dp)
          .fillMaxWidth()
      ) {
        val (replyIcon, repeatIcon, favoriteIcon,
          replyCount, repeatCount, favoriteCount) = createRefs()
        actionList.forEach {
          ClickableIcon(
            imageVector = it.key,
            tint = Color.Gray,
            modifier = Modifier
              .size(22.dp)
              .constrainAs(
                ref = when (it.key) {
                  Icons.Rounded.Reply -> replyIcon
                  Icons.Rounded.Repeat -> repeatIcon
                  else -> favoriteIcon
                }
              ) {
                start.linkTo(
                  anchor = when (it.key) {
                    Icons.Rounded.Reply -> parent.start
                    Icons.Rounded.Repeat -> replyIcon.end
                    else -> repeatIcon.end
                  },
                  margin = if (it.key == Icons.Rounded.Reply) 0.dp else 80.dp
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
                  Icons.Rounded.Reply -> replyCount
                  Icons.Rounded.Repeat -> repeatCount
                  else -> favoriteCount
                },
              ) {
                start.linkTo(
                  anchor = when (it.key) {
                    Icons.Rounded.Reply -> replyIcon.end
                    Icons.Rounded.Repeat -> repeatIcon.end
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
}
