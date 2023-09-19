package com.github.whitescent.mastify.screen.profile

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.mapper.emoji.toShortCode
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.AnimatedVisibility
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
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
import com.github.whitescent.mastify.ui.component.status.StatusSnackBar
import com.github.whitescent.mastify.ui.component.status.StatusSnackbarState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.ProfileTransitions
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
  navigator: DestinationsNavigator,
  viewModel: ProfileViewModel = hiltViewModel()
) {
  val uiState = viewModel.uiState
  val accountStatus = viewModel.pager.collectAsLazyPagingItems()
  val profileLayoutState = rememberProfileLayoutState()
  val statusListState = rememberLazyListState()
  val context = LocalContext.current
  val snackbarState = remember { StatusSnackbarState() }

  Box {
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
                modifier = Modifier
                  .graphicsLayer {
                    scaleY = (1 - profileLayoutState.progress).coerceAtLeast(0.7f)
                    scaleX = (1 - profileLayoutState.progress).coerceAtLeast(0.7f)
                  }
                  .shadow(12.dp, AppTheme.shape.avatarShape.copy(all = CornerSize(20.dp)))
                  .size(80.dp),
                shape = AppTheme.shape.avatarShape.copy(all = CornerSize(20.dp))
              )
            },
          )
          HeightSpacer(value = 6.dp)
          ProfileInfo(uiState.account, uiState.isSelf, uiState.isFollowing)
        }
      },
      enabledScroll = accountStatus.loadState.refresh is LoadState.NotLoading,
      bodyContent = {
        val tabs = listOf(ProfileTabItem.POST, ProfileTabItem.REPLY, ProfileTabItem.MEDIA)
        var selectedTab by remember { mutableStateOf(0) }
        val pagerState = rememberPagerState { tabs.size }
        val scope = rememberCoroutineScope()
        Column(
          Modifier.heightIn(
            max = when (
              accountStatus.loadState.refresh is LoadState.Loading ||
                accountStatus.loadState.refresh is LoadState.Error
            ) {
              true -> profileLayoutState.bodyContentMaxHeight
              else -> Dp.Unspecified
            }
          )
        ) {
          ProfileTabs(tabs, selectedTab) {
            if (selectedTab == 0 && it == 0) {
              scope.launch {
                statusListState.scrollToItem(0)
              }.invokeOnCompletion { profileLayoutState.animatedToTop() }
            }
            selectedTab = it
            scope.launch {
              pagerState.scrollToPage(it)
            }
          }
          ProfilePager(
            state = pagerState,
            accountStatus = accountStatus,
            statusListState = statusListState,
            action = {
              viewModel.onStatusAction(it, context)
            },
            navigateToDetail = {
              navigator.navigate(
                StatusDetailDestination(
                  avatar = uiState.account.avatar,
                  status = it
                )
              )
            },
            navigateToMedia = { attachments, targetIndex ->
              navigator.navigate(
                StatusMediaScreenDestination(attachments.toTypedArray(), targetIndex)
              )
            },
            navigateToProfile = {
              navigator.navigate(
                ProfileDestination(it)
              )
            },
          )
        }
        LaunchedEffect(pagerState) {
          snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedTab = page
          }
        }
      },
      topBar = {
        ProfileTopBar(
          alpha = profileLayoutState.progress,
          account = uiState.account,
          topPadding = appState.appPaddingValues.calculateTopPadding()
        )
      }
    )
    StatusSnackBar(
      state = snackbarState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(start = 12.dp, end = 12.dp, bottom = 36.dp)
    )
  }
  LaunchedEffect(Unit) {
    viewModel.snackBarFlow.collect {
      snackbarState.showSnackbar(it)
    }
  }
}

@Composable
fun ProfileTopBar(
  alpha: Float,
  account: Account,
  topPadding: Dp,
) {
  val defaultBackgroundColor = AppTheme.colors.defaultHeader
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp + topPadding)
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(account.header)
        .build(),
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
        .alpha(alpha)
        .drawWithContent {
          drawRect(defaultBackgroundColor)
          this.drawContent()
          drawRect(Color.Black.copy(0.35f))
        },
      contentScale = ContentScale.Crop
    )
    AnimatedVisibility(
      visible = alpha >= 1,
      enter = slideInVertically { it } + fadeIn(),
      exit = slideOutVertically { it / 2 } + fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      CenterRow(Modifier.statusBarsPadding().padding(start = 24.dp).width(280.dp)) {
        CircleShapeAsyncImage(
          model = account.avatar,
          modifier = Modifier.size(36.dp),
          shape = AppTheme.shape.avatarShape
        )
        WidthSpacer(value = 8.dp)
        Column {
          Text(
            text = buildAnnotatedString {
              annotateInlineEmojis(account.realDisplayName, account.emojis.toShortCode())
            },
            fontSize = 18.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            inlineContent = inlineTextContentWithEmoji(account.emojis, 18.sp),
          )
          Text(
            text = stringResource(id = R.string.post_count, account.statusesCount),
            fontSize = 14.sp,
            color = Color.White,
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
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  Column(Modifier.padding(horizontal = avatarStartPadding)) {
    Row(Modifier.fillMaxWidth()) {
      Column(Modifier.weight(1f)) {
        Text(
          text = buildAnnotatedString {
            annotateInlineEmojis(account.realDisplayName, account.emojis.toShortCode())
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
        text = account.noteWithEmoji,
        style = TextStyle(fontSize = 16.sp, color = AppTheme.colors.primaryContent),
        onLinkClick = {
          launchCustomChromeTab(
            context = context,
            uri = Uri.parse(it),
            toolbarColor = primaryColor.toArgb(),
          )
        }
      )
    }
    HeightSpacer(value = 8.dp)
    AccountFields(
      account.fieldsWithEmoji,
      account.followingCount,
      account.followersCount,
      account.statusesCount
    )
  }
}

@Composable
fun ProfileTabs(
  tabs: List<ProfileTabItem>,
  selectedTab: Int,
  onTabClick: (Int) -> Unit
) {
  TabRow(
    selectedTabIndex = selectedTab,
    indicator = {
      TabRowDefaults.PrimaryIndicator(
        modifier = Modifier.tabIndicatorOffset(it[selectedTab]),
        width = 40.dp,
        height = 5.dp,
        color = AppTheme.colors.accent
      )
    },
    containerColor = Color.Transparent,
    divider = {
      AppHorizontalDivider(thickness = 1.dp)
    },
  ) {
    tabs.forEachIndexed { index, tab ->
      val selected = selectedTab == index
      Tab(
        selected = selected,
        onClick = {
          onTabClick(index)
        },
        modifier = Modifier.clip(RoundedCornerShape(18.dp))
      ) {
        Text(
          text = stringResource(
            id = when (tab) {
              ProfileTabItem.POST -> R.string.post_title
              ProfileTabItem.REPLY -> R.string.reply_title
              else -> R.string.media_title
            }
          ),
          fontSize = 17.sp,
          fontWeight = FontWeight(700),
          color = if (selected) AppTheme.colors.primaryContent else AppTheme.colors.secondaryContent,
          modifier = Modifier.padding(12.dp),
        )
      }
    }
  }
}

@Composable
fun AccountFields(
  fields: List<Fields>,
  followingCount: Long,
  followersCount: Long,
  statusesCount: Long,
) {
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  val icons by remember(followersCount, followersCount, statusesCount) {
    mutableStateOf(
      listOf(
        ProfileInfoItem(R.drawable.following, followingCount),
        ProfileInfoItem(R.drawable.follower, followersCount),
        ProfileInfoItem(R.drawable.status, statusesCount),
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
            onLinkClick = { url ->
              launchCustomChromeTab(
                context = context,
                uri = Uri.parse(url),
                toolbarColor = primaryColor.toArgb(),
              )
            }
          )
        }
        if (it != fields.last()) HeightSpacer(value = 4.dp)
      }
      HeightSpacer(value = 10.dp)
    }
    CenterRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      icons.forEachIndexed { index, item ->
        CenterRow {
          Icon(
            painter = painterResource(id = item.icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppTheme.colors.primaryContent.copy(0.7f)
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = stringResource(
              id = when (index) {
                0 -> R.string.following_count
                1 -> R.string.follower_count
                else -> R.string.post_count
              },
              item.itemCount
            ),
            color = AppTheme.colors.primaryContent.copy(0.7f),
            fontSize = 16.sp,
            fontWeight = FontWeight(650),
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
data class ProfileInfoItem(
  @DrawableRes val icon: Int,
  val itemCount: Long
)

enum class ProfileTabItem {
  POST, REPLY, MEDIA
}
