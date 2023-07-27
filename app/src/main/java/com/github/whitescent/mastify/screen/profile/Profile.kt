package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.ui.component.AvatarWithCover
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.avatarStartPadding
import com.github.whitescent.mastify.ui.component.htmlText.HtmlText
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import com.ramcosta.composedestinations.annotation.Destination

data class ProfileNavArgs(
  val account: Account
)

@AppNavGraph
@Destination(navArgsDelegate = ProfileNavArgs::class)
@Composable
fun Profile(
  viewModel: ProfileViewModel = hiltViewModel()
) {
  val uiState = viewModel.uiState
  Column(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
    AvatarWithCover(
      cover = {
        if (uiState.account.header.contains("missing.png")) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .background(AppTheme.colors.accent10),
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
          modifier = Modifier.size(100.dp)
        )
      },
    )
    HeightSpacer(value = 8.dp)
    ProfileInfo(
      uiState.account,
      uiState.isSelf,
      uiState.isFollowing
    )
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
          text = account.realDisplayName,
          fontSize = 24.sp,
          fontWeight = FontWeight(500)
        )
        HeightSpacer(value = 2.dp)
        Text(
          text = account.username,
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
    HeightSpacer(value = 8.dp)
    HtmlText(
      text = account.note,
      style = TextStyle(fontSize = 16.sp)
    )
    HeightSpacer(value = 8.dp)
    AccountFields(account.fields)
  }
}

@Composable
fun AccountFields(fields: List<Fields>) {
  if (fields.isEmpty()) return
  Column(Modifier.fillMaxWidth()) {
    fields.forEach {
      CenterRow {
        Box(Modifier.width(100.dp), Alignment.CenterStart) {
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
        HtmlText(text = it.value, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight(450)))
      }
      if (it != fields.last()) HeightSpacer(value = 4.dp)
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
    CenterRow(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
      Icon(
        painter = painterResource(id = R.drawable.pencil_simple_line),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = Color.White
      )
      WidthSpacer(value = 8.dp)
      Text(
        text = "编辑个人资料",
        fontSize = 16.sp,
        color = Color.White
      )
    }
  }
}
