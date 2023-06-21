package com.github.whitescent.mastify.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.BlurTransformation

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppDrawer(
  drawerState: DrawerState,
  activeAccount: AccountEntity,
  accounts: MutableList<AccountEntity>,
  changeAccount: (Long) -> Unit,
  navigateToLogin: () -> Unit,
) {
  val context = LocalContext.current
  var expanded by rememberSaveable { mutableStateOf(false) }
  val rotate by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

  ModalDrawerSheet(
    windowInsets = WindowInsets(0, 0, 0,0),
    drawerContainerColor = AppTheme.colors.background
  ) {
    Box(
      modifier = Modifier
        .heightIn(max = 200.dp)
    ) {
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(activeAccount.header)
          .crossfade(true)
          .transformations(BlurTransformation(context, radius = 2f))
          .build(),
        contentDescription = null,
        modifier = Modifier
          .fillMaxSize()
          .alpha(0.6f),
        contentScale = ContentScale.Crop,
      )
      Column(
        modifier = Modifier
          .statusBarsPadding()
          .padding(horizontal = 24.dp, vertical = 8.dp)
      ) {
        CircleShapeAsyncImage(
          model = activeAccount.profilePictureUrl,
          modifier = Modifier.size(72.dp)
        )
        HeightSpacer(value = 6.dp)
        CenterRow {
          Column(
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = activeAccount.displayName,
              fontSize = 24.sp,
              color = AppTheme.colors.primaryContent
            )
            Text(
              text = activeAccount.fullName,
              fontSize = 16.sp,
              color = AppTheme.colors.primaryContent
            )
          }
          IconButton(
            onClick = {
              expanded = !expanded
            }
          ) {
            Icon(
              painter = painterResource(R.drawable.more_arrow),
              contentDescription = null,
              modifier = Modifier.rotate(rotate),
              tint = AppTheme.colors.primaryContent
            )
          }
        }
      }
    }
    AnimatedContent(
      targetState = expanded,
      transitionSpec = {
        slideInVertically(tween(600)) togetherWith
          slideOutVertically(tween(600))
      }
    ) {
      when (it) {
        true -> {
          Surface(
            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
            modifier = Modifier.fillMaxWidth(),
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
                      onClick = { changeAccount(account.id) },
                    )
                    .padding(16.dp),
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
                    Icon(
                      painter = painterResource(id = R.drawable.check_circle),
                      contentDescription = null,
                      tint = Color(0xFF4dcb5e),
                      modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
                    )
                  }
                }
              }
              CenterRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = rememberRipple(
                      bounded = true,
                      radius = 250.dp,
                    ),
                    onClick = navigateToLogin
                  )
                  .padding(horizontal = 16.dp, vertical = 8.dp)
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
                  text = "添加账号",
                  fontSize = 16.sp,
                  fontWeight = FontWeight(500),
                  color = AppTheme.colors.primaryContent,
                )
              }
              Divider(thickness = 0.5.dp)
            }
          }
        }
        else -> Unit
      }
    }
  }

  LaunchedEffect(drawerState.isOpen) {
    if (!drawerState.isOpen) {
      expanded = false
    }
  }

}
