/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.ui.component

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DrawerState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
import com.github.whitescent.mastify.mapper.toAccount
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
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
      modifier = Modifier.heightIn(max = 200.dp),
      contentAlignment = Alignment.Center
    ) {
      when (activeAccount.isEmptyHeader) {
        true -> Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.defaultHeader))
        else -> {
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
        }
      }
      Column(
        modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp)
      ) {
        CircleShapeAsyncImage(
          model = activeAccount.profilePictureUrl,
          modifier = Modifier.size(72.dp),
          onClick = {
            navigateToProfile(activeAccount.toAccount())
          },
          shape = AppTheme.shape.mediumAvatar
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
              text = activeAccount.fullname,
              fontSize = 16.sp,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
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
        modifier = Modifier.onGloballyPositioned {
          accountListHeight = it.size.height
        },
        color = AppTheme.colors.background
      ) {
        Column {
          accounts.forEach { account ->
            CenterRow(
              modifier = Modifier
                .clickable {
                  if (account != activeAccount) {
                    changeAccount(account.id)
                  } else {
                    scope.launch {
                      drawerState.close()
                    }
                  }
                }
                .padding(12.dp),
            ) {
              CircleShapeAsyncImage(
                model = account.profilePictureUrl,
                modifier = Modifier.size(40.dp),
                shape = AppTheme.shape.smallAvatar
              )
              WidthSpacer(value = 8.dp)
              Text(
                text = account.fullname,
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
              .clickable {
                navigateToLogin()
              }
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
          AppHorizontalDivider()
        }
      }
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
private fun DrawerMenu() {
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  AppDrawerMenu.entries.forEach {
    if (it.route == AppDrawerMenu.Settings.route) {
      AppHorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
    DrawerMenuItem(it.icon, it.redId) {
      when (it.route) {
        "about" -> {
          launchCustomChromeTab(
            context = context,
            uri = Uri.parse("https://github.com/whitescent/Mastify"),
            toolbarColor = primaryColor.toArgb(),
          )
        }
      }
    }
  }
}

@Composable
private fun DrawerMenuItem(icon: Int, name: Int, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
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
  Favorites(R.drawable.heart, R.string.title_favorites, "favorites"),
  Lists(R.drawable.list_bullets, R.string.title_lists, "lists"),
  Drafts(R.drawable.scroll, R.string.title_draft, "draft"),
  Settings(R.drawable.gear, R.string.title_settings, "settings"),
  About(R.drawable.info, R.string.title_about_mastify, "about"),
  Logout(R.drawable.sign_out, R.string.title_logout, "logout")
}

private fun Modifier.drawerListItemPadding(): Modifier =
  this.padding(horizontal = 16.dp, vertical = 6.dp)
