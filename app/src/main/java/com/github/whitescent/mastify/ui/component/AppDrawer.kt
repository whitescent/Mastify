package com.github.whitescent.mastify.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.mapper.account.toAccount
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.ui.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
  isSystemBarVisible: Boolean,
  drawerState: DrawerState,
  activeAccount: AccountEntity,
  accounts: ImmutableList<AccountEntity>,
  changeAccount: (Long) -> Unit,
  navigateToLogin: () -> Unit,
  navigateToProfile: (Account) -> Unit,
) {
  val scope = rememberCoroutineScope()
  ModalDrawerSheet(
    windowInsets = WindowInsets(0, 0, 0, 0),
    drawerContainerColor = AppTheme.colors.background
  ) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotate by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    val transition = updateTransition(targetState = expanded)
    var accountListHeight by remember { mutableIntStateOf(0) }
    val animatedHeight by transition.animateInt {
      when (it) {
        true -> 0
        else -> -accountListHeight
      }
    }
    Box(
      Modifier.heightIn(max = 200.dp)
    ) {
      AsyncImage(
        model = activeAccount.header,
        contentDescription = null,
        modifier = Modifier
          .fillMaxSize()
          .drawWithContent {
            this.drawContent()
            drawRect(Color.Black.copy(0.35f))
          },
        contentScale = ContentScale.Crop,
      )
      Column(
        modifier = Modifier
          .let {
            if (isSystemBarVisible) {
              // Ensure correct padding is maintained
              // when using this app in environments similar to WSA
              it
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
            } else {
              it.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 8.dp)
            }
          },
      ) {
        CircleShapeAsyncImage(
          model = activeAccount.profilePictureUrl,
          modifier = Modifier.size(72.dp),
          onClick = {
            navigateToProfile(activeAccount.toAccount())
          }
        )
        HeightSpacer(value = 6.dp)
        CenterRow {
          Column(
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = activeAccount.realDisplayName,
              fontSize = 24.sp,
              color = Color.White
            )
            Text(
              text = activeAccount.fullName,
              fontSize = 16.sp,
              color = Color.White
            )
          }
          IconButton(
            onClick = { expanded = !expanded }
          ) {
            Icon(
              painter = painterResource(R.drawable.more_arrow),
              contentDescription = null,
              modifier = Modifier.rotate(rotate),
              tint = Color.White
            )
          }
        }
      }
    }

    Column(
      modifier = Modifier
        .clipToBounds()
        .offset { IntOffset(0, animatedHeight) }
    ) {
      Surface(
        modifier = Modifier
          .onGloballyPositioned {
            accountListHeight = it.size.height
          },
        color = AppTheme.colors.background
      ) {
        Column {
          accounts.forEach { account ->
            CenterRow(
              modifier = Modifier
                .clickable(
                  interactionSource = MutableInteractionSource(),
                  indication = rememberRipple(
                    bounded = true,
                    radius = 250.dp,
                  ),
                  onClick = {
                    if (account != activeAccount) changeAccount(account.id)
                    else {
                      scope.launch {
                        drawerState.close()
                      }
                    }
                  },
                )
                .padding(12.dp),
            ) {
              CircleShapeAsyncImage(
                model = account.profilePictureUrl,
                modifier = Modifier.size(40.dp)
              )
              WidthSpacer(value = 8.dp)
              Text(
                text = account.fullName,
                fontSize = 16.sp,
                color = AppTheme.colors.primaryContent,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight(500),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
              )
              if (account == activeAccount) {
                Image(
                  painter = painterResource(id = R.drawable.check_circle_fill),
                  contentDescription = null,
                  modifier = Modifier
                    .size(32.dp)
                    .padding(start = 4.dp)
                )
              }
            }
          }
          CenterRow(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                  bounded = true,
                  radius = 250.dp,
                ),
                onClick = navigateToLogin
              )
              .drawerListItemPadding()
          ) {
            Box(
              modifier = Modifier.size(40.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AppTheme.colors.primaryContent
              )
            }
            WidthSpacer(value = 8.dp)
            Text(
              text = stringResource(id = R.string.title_add_account),
              fontSize = 16.sp,
              fontWeight = FontWeight(500),
              color = AppTheme.colors.primaryContent,
            )
          }
          Divider(thickness = 0.5.dp)
        }
      }
      HeightSpacer(value = 8.dp)
      DrawerMenu()
    }

    LaunchedEffect(drawerState.isOpen) {
      if (!drawerState.isOpen) {
        expanded = false
      }
    }
  }
}

@Composable
fun DrawerMenu() {
  AppDrawerMenu.values().forEach {
    if (it.route == AppDrawerMenu.Settings.route) {
      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
    }
    DrawerMenuItem(it.icon, it.redId) { }
  }
}

@Composable
fun DrawerMenuItem(icon: Int, name: Int, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = rememberRipple(
          bounded = true,
          radius = 250.dp,
        ),
        onClick = onClick,
      )
  ) {
    CenterRow(
      modifier = Modifier.drawerListItemPadding()
    ) {
      Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = icon),
          contentDescription = null,
          modifier = Modifier.size(24.dp),
          tint = AppTheme.colors.primaryContent,
        )
      }
      WidthSpacer(value = 8.dp)
      Text(
        text = stringResource(id = name),
        fontSize = 18.sp,
        color = AppTheme.colors.primaryContent,
      )
    }
  }
}

enum class AppDrawerMenu(
  @DrawableRes val icon: Int,
  @StringRes val redId: Int,
  val route: String,
) {
  Profile(R.drawable.user, R.string.title_profile, "profile"),
  Bookmarks(R.drawable.bookmark_simple, R.string.title_bookmarks, "bookmarks"),
  Favorites(R.drawable.heart2, R.string.title_favorites, "favorites"),
  Lists(R.drawable.list_bullets, R.string.title_lists, "lists"),
  Drafts(R.drawable.scroll, R.string.title_draft, "draft"),
  Settings(R.drawable.gear, R.string.title_settings, "settings"),
  About(R.drawable.info, R.string.title_about, "about"),
  Logout(R.drawable.sign_out, R.string.title_logout, "logout")
}

private fun Modifier.drawerListItemPadding(): Modifier =
  this.padding(horizontal = 16.dp, vertical = 6.dp)
