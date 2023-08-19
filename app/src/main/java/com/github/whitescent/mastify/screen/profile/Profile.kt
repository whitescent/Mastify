package com.github.whitescent.mastify.screen.profile

import androidx.annotation.DrawableRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.mapper.emoji.toShortCode
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.ui.component.AnimatedVisibility
import com.github.whitescent.mastify.ui.component.AvatarWithCover
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.annotateInlineEmojis
import com.github.whitescent.mastify.ui.component.avatarStartPadding
import com.github.whitescent.mastify.ui.component.inlineTextContentWithEmoji
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.ProfileLayout
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.rememberProfileLayoutState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.ProfileTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.BlurTransformation
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

data class ProfileNavArgs(
  val account: Account
)

@OptIn(ExperimentalFoundationApi::class)
@AppNavGraph
@Destination(navArgsDelegate = ProfileNavArgs::class, style = ProfileTransitions::class)
@Composable
fun Profile(
  appState: AppState,
  viewModel: ProfileViewModel = hiltViewModel()
) {
  val uiState = viewModel.uiState
  val accountStatus = viewModel.pager.collectAsLazyPagingItems()
  val profileLayoutState = rememberProfileLayoutState()
  ProfileLayout(
    state = profileLayoutState,
    collapsingTop = {
      Column {
        AvatarWithCover(
          cover = {
            if (uiState.account.header.contains("missing.png")) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(200.dp)
                  .background(AppTheme.colors.defaultHeader),
              )
            } else {
              AsyncImage(
                model = uiState.account.header,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp),
              )
            }
          },
          avatar = {
            CircleShapeAsyncImage(
              model = uiState.account.avatar,
              modifier = Modifier.size(80.dp * (1 - profileLayoutState.progress))
            )
          },
        )
        HeightSpacer(value = 6.dp)
        ProfileInfo(uiState.account, uiState.isSelf, uiState.isFollowing)
      }
    },
    bodyContent = {
      val tabs = listOf(ProfileTabItem.POST, ProfileTabItem.REPLY, ProfileTabItem.MEDIA)
      val pagerState = rememberPagerState { tabs.size }
      val scope = rememberCoroutineScope()
      Column {
        ProfileTabs(tabs) {
          scope.launch {
            pagerState.scrollToPage(it)
          }
        }
        ProfilePager(state = pagerState, accountStatus = accountStatus)
      }
    },
    topBar = {
      ProfileTopBar(
        alpha = profileLayoutState.progress,
        account = uiState.account,
        topPadding = appState.appPaddingValues.calculateTopPadding()
      )
    },
  )
}

@Composable
fun ProfileTopBar(
  alpha: Float,
  account: Account,
  topPadding: Dp,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp + topPadding)
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(account.header)
        .transformations(BlurTransformation(LocalContext.current))
        .build(),
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
        .alpha(alpha)
        .drawWithContent {
          this.drawContent()
          drawRect(Color.Black.copy(0.35f))
        },
      contentScale = ContentScale.Crop
    )
    AnimatedVisibility(
      visible = alpha >= 1,
      enter = scaleIn() + fadeIn(),
      exit = scaleOut() + fadeOut()
    ) {
      CenterRow(Modifier.statusBarsPadding().padding(start = 24.dp).width(240.dp)) {
        CircleShapeAsyncImage(
          model = account.avatar,
          modifier = Modifier.size(36.dp)
        )
        WidthSpacer(value = 8.dp)
        Column {
          Text(
            text = account.realDisplayName,
            fontSize = 18.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${account.statusesCount} 条嘟文",
            fontSize = 14.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
fun ProfileInfo(
  account: Account,
  isSelf: Boolean,
  isFollowing: Boolean?
) {
  Column(Modifier.padding(horizontal = avatarStartPadding)) {
    Row(Modifier.fillMaxWidth()) {
      Column(Modifier.weight(1f)) {
        Text(
          text = buildAnnotatedString {
            annotateInlineEmojis(account.realDisplayName, account.emojis.toShortCode(), this)
          },
          fontSize = 24.sp,
          fontWeight = FontWeight(500),
          color = AppTheme.colors.primaryContent,
          inlineContent = inlineTextContentWithEmoji(account.emojis, 24.sp),
        )
        HeightSpacer(value = 2.dp)
        Text(
          text = account.fullname,
          style = AppTheme.typography.statusUsername.copy(
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
          ),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          fontSize = 16.sp,
        )
      }
      when (isSelf) {
        true -> EditProfileButton()
        else -> {
          isFollowing?.let {
            FollowButton(it)
          }
        }
      }
    }
    if (account.note.isNotEmpty()) {
      HeightSpacer(value = 8.dp)
      HtmlText(
        text = account.note,
        style = TextStyle(fontSize = 16.sp, color = AppTheme.colors.primaryContent),
      )
    }
    HeightSpacer(value = 8.dp)
    AccountFields(
      account.emojis,
      account.fields,
      account.followingCount,
      account.followersCount,
      account.statusesCount
    )
  }
}

@Composable
fun ProfileTabs(
  tabs: List<ProfileTabItem>,
  onTabClick: (Int) -> Unit
) {
  var state by remember { mutableIntStateOf(0) }
  TabRow(
    selectedTabIndex = state,
    indicator = {
      TabRowDefaults.PrimaryIndicator(
        modifier = Modifier.tabIndicatorOffset(it[state]),
        width = 40.dp,
        height = 5.dp,
        color = AppTheme.colors.accent
      )
    },
    containerColor = Color.Transparent,
  ) {
    tabs.forEachIndexed { index, tab ->
      val selected = state == index
      Tab(
        selected = selected,
        onClick = {
          state = index
          onTabClick(index)
        }
      ) {
        Text(
          text = when (tab) {
            ProfileTabItem.POST -> "嘟文"
            ProfileTabItem.REPLY -> "回复"
            else -> "媒体"
          },
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
          color = if (selected) AppTheme.colors.primaryContent else AppTheme.colors.secondaryContent,
          modifier = Modifier.padding(12.dp)
        )
      }
    }
  }
}

@Composable
fun AccountFields(
  accountEmojis: List<Emoji>,
  fields: List<Fields>,
  followingCount: Long,
  followersCount: Long,
  statusesCount: Long,
) {
  val icons by remember(followersCount, followersCount, statusesCount) {
    mutableStateOf(
      listOf(
        ProfileButton(R.drawable.following, "$followingCount 正在关注"),
        ProfileButton(R.drawable.follower, "$followersCount 关注者"),
        ProfileButton(R.drawable.status, "$statusesCount 嘟文"),
      )
    )
  }
  Column(Modifier.fillMaxWidth()) {
    if (fields.isNotEmpty()) {
      fields.forEach {
        CenterRow(Modifier.fillMaxWidth()) {
          Box(Modifier.width(150.dp), Alignment.CenterStart) {
            CenterRow {
              Text(
                text = it.name,
                color = Color(0xFF8B8B8B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              it.verifiedAt?.let {
                WidthSpacer(value = 4.dp)
                Icon(
                  painter = painterResource(id = R.drawable.seal_check),
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = Color(0xFF00BA7C)
                )
              }
            }
          }
          Spacer(Modifier.weight(1f).padding(horizontal = 154.dp))
          HtmlText(
            text = it.value,
            maxLines = 1,
            fontSize = 16.sp,
            fontWeight = FontWeight(450),
            overflow = TextOverflow.Ellipsis,
            inlineContent = inlineTextContentWithEmoji(accountEmojis),
          )
        }
        if (it != fields.last()) HeightSpacer(value = 4.dp)
      }
      HeightSpacer(value = 10.dp)
    }
    CenterRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      icons.forEach {
        CenterRow {
          Icon(
            painter = painterResource(id = it.icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppTheme.colors.primaryContent.copy(0.7f)
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = it.text,
            color = AppTheme.colors.primaryContent.copy(0.7f),
            fontSize = 16.sp,
            fontWeight = FontWeight(650)
          )
        }
      }
    }
  }
}

@Composable
fun FollowButton(isFollowing: Boolean) {
  Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isFollowing) AppTheme.colors.unfollowButton
      else AppTheme.colors.followButton
    ),
    border = BorderStroke(1.dp, Color(0xFFA1A1A1).copy(0.6f)),
    modifier = Modifier.width(140.dp),
    shape = RoundedCornerShape(12.dp)
  ) {
    Text(
      text = if (isFollowing) "正在关注" else "关注",
      color = if (isFollowing) AppTheme.colors.primaryContent else AppTheme.colors.bottomBarBackground,
      fontSize = 16.sp
    )
  }
}

@Composable
fun EditProfileButton() {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = AppTheme.colors.secondaryContent
  ) {
    CenterRow(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
      Icon(
        painter = painterResource(id = R.drawable.pencil_simple_line),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = Color.White
      )
      WidthSpacer(value = 6.dp)
      Text(
        text = "编辑个人资料",
        fontSize = 16.sp,
        color = Color.White
      )
    }
  }
}

@Immutable
data class ProfileButton(
  @DrawableRes val icon: Int,
  val text: String
)

enum class ProfileTabItem {
  POST, REPLY, MEDIA
}
