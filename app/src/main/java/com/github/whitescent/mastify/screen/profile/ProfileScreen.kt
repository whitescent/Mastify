package com.github.whitescent.mastify.screen.profile

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.BottomBarNavGraph
import com.github.whitescent.mastify.data.model.AccountModel
import com.github.whitescent.mastify.destinations.EditProfileScreenDestination
import com.github.whitescent.mastify.network.model.response.account.Profile
import com.github.whitescent.mastify.screen.profile.pager.ProfilePager
import com.github.whitescent.mastify.ui.component.*
import com.github.whitescent.mastify.ui.component.nestedscrollview.VerticalNestedScrollView
import com.github.whitescent.mastify.ui.component.nestedscrollview.rememberNestedScrollViewState
import com.github.whitescent.mastify.utils.FormatFactory
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@BottomBarNavGraph
@Destination
@Composable
fun ProfileScreen(
  navController: NavController,
  viewModel: ProfileScreenModel = hiltViewModel()
) {
  Crossfade(targetState = viewModel.account) {
    when (it) {
      null -> CenterCircularProgressIndicator()
      else -> {
        val nestedScrollViewState = rememberNestedScrollViewState()
        VerticalNestedScrollView(
          state = nestedScrollViewState,
          header = {
            ProfileHeader(account = viewModel.account!!, profile = viewModel.profile, navController)
          },
          content = {
            Column {
              val tabs = listOf(ProfileTabItem.POST, ProfileTabItem.ABOUT)
              val pagerState = rememberPagerState()
              val scope = rememberCoroutineScope()
              ProfileTab(
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
                aboutModel = viewModel.profile
              )
            }
          },
        )
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
  account: AccountModel,
  profile: Profile?,
  navController: NavController
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
          .height(200.dp),
        contentScale = ContentScale.Crop
      )
      Column {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp)
            .height(130.dp)
        ) {
          CircleShapeAsyncImage(
            model = account.avatar,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Crop,
            border = BorderStroke(4.dp, Color.White)
          )
          CenterRow(
            modifier = Modifier
              .padding(end = 24.dp)
              .align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
          ) {
            AccountInfo(number = account.statusesCount, description = "嘟文")
            AccountInfo(number = account.followersCount, description = "关注者")
            AccountInfo(number = account.followingCount, description = "正在关注")
          }
        }
      }
    }
    Column(modifier = Modifier
      .padding(start = 14.dp)
      .fillMaxWidth()) {
      CenterRow(modifier = Modifier.fillMaxWidth()) {
        Column {
          Text(
            text = account.username,
            style = AppTheme.typography.headlineSmall,
            color = AppTheme.colorScheme.onBackground
          )
          HeightSpacer(value = 2.dp)
          Text(
            text = "@${account.username}@${account.instanceName}",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onBackground.copy(alpha = 0.5f)
          )
        }
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.CenterEnd
        ) {
          Button(
            onClick = { navController.navigate(EditProfileScreenDestination) },
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(Icons.Rounded.Edit, null)
            WidthSpacer(value = 4.dp)
            Text(text = "编辑资料")
          }
        }
      }
      HeightSpacer(value = 2.dp)
      Text(
        text = account.note,
        style = AppTheme.typography.bodyLarge.copy(
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        ),
        overflow = TextOverflow.Ellipsis
      )
      AnimatedVisibility(
        visible = profile != null,
        enter = fadeIn(tween(easing = LinearEasing))
      ) {
        Column {
          HeightSpacer(value = 4.dp)
          CenterRow {
            Icon(
              imageVector = Icons.Rounded.CalendarMonth,
              contentDescription = null,
              tint = Color.Gray,
              modifier = Modifier.size(22.dp)
            )
            WidthSpacer(value = 4.dp)
            Text(
              text = "${FormatFactory.getTimeYear(profile!!.createdAt)}年" +
                "${FormatFactory.getTimeMouth(profile.createdAt)}月加入",
              style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.colorScheme.onBackground.copy(alpha = 0.7f),
              )
            )
          }
        }
      }
    }
    HeightSpacer(value = 12.dp)
  }
}

@Composable
fun ProfileTab(
  tabs: List<ProfileTabItem>,
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
            ProfileTabItem.POST -> {
              CenterRow(modifier = Modifier.padding(4.dp)) {
                Icon(
                  painter = painterResource(
                    id = when (selectedTabIndex == index) {
                      true -> R.drawable.rows_fill
                      false -> R.drawable.rows
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
            ProfileTabItem.ABOUT -> {
              CenterRow(modifier = Modifier.padding(4.dp)) {
                Icon(
                  painter = painterResource(
                    id = when (selectedTabIndex == index) {
                      true -> R.drawable.user_fill
                      false -> R.drawable.user
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

enum class ProfileTabItem {
  POST, ABOUT
}
