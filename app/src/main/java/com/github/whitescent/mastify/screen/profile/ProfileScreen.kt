package com.github.whitescent.mastify.screen.profile

import android.util.TypedValue
import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.data.model.AccountModel
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.ui.component.*
import com.github.whitescent.mastify.ui.component.nestedscrollview.VerticalNestedScrollView
import com.github.whitescent.mastify.ui.component.nestedscrollview.rememberNestedScrollViewState
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.getInstanceName
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
  viewModel: ProfileScreenModel = hiltViewModel()
) {

  BoxWithConstraints {
    Crossfade(targetState = viewModel.account) {
      when (it) {
        null -> CenterCircularProgressIndicator()
        else -> {
          val nestedScrollViewState = rememberNestedScrollViewState()

          VerticalNestedScrollView(
            state = nestedScrollViewState,
            header = {
              ProfileHeader(
                account = viewModel.account!!
              )
            },
            content = {
              Column {
                val tabs = listOf(ProfileTab.POST, ProfileTab.ABOUT)
                val pagerState = rememberPagerState()
                val scope = rememberCoroutineScope()
                ProfileTabView(
                  tabs = tabs,
                  selectedTabIndex = pagerState.currentPage,
                  onTabClick = { index ->
                    scope.launch {
                      pagerState.animateScrollToPage(index)
                    }
                  }
                )
                ProfilePager(
                  state = pagerState,
                  statuses = viewModel.statuses,
                  instanceName = viewModel.account!!.instanceName,
                )
              }
            }
          )
        }
      }
    }
  }

  DisposableEffect(Unit) {
    viewModel.initProfilePage()
    onDispose { }
  }

}

@Composable
fun ProfileHeader(
  account: AccountModel
) {
  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    Box {
      AsyncImage(
        model = account.header,
        contentDescription = null,
        modifier = Modifier
          .fillMaxWidth()
          .height(265.dp),
        contentScale = ContentScale.Crop
      )
      Column {
        Box(
          Modifier
            .fillMaxWidth()
            .height(200.dp)
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
          contentAlignment = Alignment.Center
        ) {
          CircleShapeAsyncImage(
            model = account.avatar,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Crop,
            border = BorderStroke(4.dp, Color.White)
          )
        }
      }
    }
    Column {
      HeightSpacer(value = 6.dp)
      CenterRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = account.username,
          style = AppTheme.typography.headlineSmall,
          color = AppTheme.colorScheme.onBackground
        )
        WidthSpacer(value = 6.dp)
        Text(
          text = "@${account.username}@${account.instanceName}",
          style = AppTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
      }
      HeightSpacer(value = 6.dp)
      Text(
        text = account.note,
        style = AppTheme.typography.bodyLarge.copy(
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        ),
        modifier = Modifier.align(Alignment.CenterHorizontally),
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
      HeightSpacer(value = 6.dp)
      CenterRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Button(
          onClick = { /*TODO*/ }
        ) {
          Icon(Icons.Rounded.Edit, null)
          WidthSpacer(value = 4.dp)
          Text(text = "编辑资料")
        }
        Button(
          onClick = { /*TODO*/ }
        ) {
          Icon(Icons.Rounded.Share, null)
        }
      }
      HeightSpacer(value = 6.dp)
      Column {
        Divider(modifier = Modifier.fillMaxWidth(), thickness = (0.5).dp)
        CenterRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          AccountInfo(number = account.statusesCount, description = "嘟文")
          AccountInfo(number = account.followersCount, description = "关注者")
          AccountInfo(number = account.followingCount, description = "正在关注")
        }
        Divider(modifier = Modifier.fillMaxWidth(), thickness = (0.5).dp)
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePager(
  state: PagerState,
  statuses: List<Status>,
  instanceName: String
) {
  HorizontalPager(
    pageCount = 2,
    modifier = Modifier
      .fillMaxHeight(),
    state = state
  ) { page ->
    Crossfade(targetState = page) {
      when (it) {
        0 -> {
          Crossfade(targetState = statuses) { list ->
            when (list.size) {
              0 -> CenterCircularProgressIndicator()
              else -> {
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
            }
          }
        }
        1 -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Gray),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "关于")
          }
        }
      }
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
            modifier = Modifier.padding(bottom = 4.dp)
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

@Composable
fun ProfileTabView(
  tabs: List<ProfileTab>,
  selectedTabIndex: Int,
  onTabClick: (Int) -> Unit
) {
  TabRow(
    selectedTabIndex = selectedTabIndex,
    containerColor = AppTheme.colorScheme.background,
    contentColor = AppTheme.colorScheme.primary
  ) {
    tabs.forEachIndexed { index, title ->
      Tab(
        selected = selectedTabIndex == index,
        onClick = {
          onTabClick(index)
        },
        text = {
          when (title) {
            ProfileTab.POST -> {
              CenterRow(modifier = Modifier.padding(12.dp)) {
                Icon(
                  painter = painterResource(
                    id = when (selectedTabIndex == index) {
                      true -> R.drawable.grid_view_selected
                      false -> R.drawable.grid_view
                    }
                  ),
                  contentDescription = null,
                  tint = AppTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
                WidthSpacer(value = 6.dp)
                Text(
                  text = "嘟文",
                  style = AppTheme.typography.titleMedium,
                  color = AppTheme.colorScheme.primary
                )
              }
            }
            ProfileTab.ABOUT -> {
              CenterRow(modifier = Modifier.padding(12.dp)) {
                Icon(
                  painter = painterResource(
                    id = when (selectedTabIndex == index) {
                      true -> R.drawable.profile_selected
                      false -> R.drawable.profile
                    }
                  ),
                  contentDescription = null,
                  tint = AppTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
                WidthSpacer(value = 6.dp)
                Text(
                  text = "关于",
                  style = AppTheme.typography.titleMedium,
                  color = AppTheme.colorScheme.primary
                )
              }
            }
          }
        }
      )
    }
  }
}

@Composable
fun AccountInfo(
  number: Long,
  description: String
) {
  Column(
    modifier = Modifier
      .clickable(
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = { }
      ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = number.toString(),
      style = AppTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = AppTheme.colorScheme.onBackground
    )
    Text(
      text = description,
      style = AppTheme.typography.titleSmall,
      color = AppTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
  }
}

@Composable
fun MyHtmlText(
  text: String,
  modifier: Modifier = Modifier,
  style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current
) {
  AndroidView(
    modifier = modifier,
    factory = { context -> TextView(context) },
    update = { view ->
      view.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
      view.setTextColor(android.graphics.Color.rgb(style.color.red, style.color.green, style.color.blue))
      view.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.fontSize.value)
    }
  )
}

enum class ProfileTab {
  POST, ABOUT
}
