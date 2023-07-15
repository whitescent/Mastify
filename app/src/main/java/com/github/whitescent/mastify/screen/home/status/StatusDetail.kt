package com.github.whitescent.mastify.screen.home.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.status.StatusDetailCard
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.StatusTransitions
import com.github.whitescent.mastify.viewModel.StatusDetailViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@AppNavGraph
@Destination(style = StatusTransitions::class)
@Composable
fun StatusDetail(
  avatar: String,
  status: Status,
  navigator: DestinationsNavigator,
  viewModel: StatusDetailViewModel = hiltViewModel()
) {
  val state = viewModel.uiState
  Column(Modifier.fillMaxSize().imePadding()) {
    Spacer(Modifier.statusBarsPadding())
    CenterRow(Modifier.padding(12.dp)) {
      IconButton(onClick = { navigator.popBackStack() }) {
        Icon(
          painter = painterResource(id = R.drawable.arrow_left),
          contentDescription = null,
          modifier = Modifier.size(28.dp),
          tint = AppTheme.colors.primaryContent
        )
      }
      WidthSpacer(value = 8.dp)
      Text(
        text = "主页",
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.primaryContent
      )
    }
    LazyColumn(Modifier.fillMaxSize().padding(bottom = 24.dp)) {
      item {
        StatusDetailCard(
          status = status,
          favouriteStatus = { viewModel.favoriteStatus(status.threadId) },
          unfavouriteStatus = { viewModel.unfavoriteStatus(status.threadId) },
          navigateToMedia = { attachments, index ->
            navigator.navigate(
              StatusMediaScreenDestination(
                attachments = attachments.toTypedArray(),
                targetMediaIndex = index
              )
            )
          },
          contentTextStyle = TextStyle(
            fontSize = 16.sp,
            color = AppTheme.colors.primaryContent
          ),
          backgroundColor = AppTheme.colors.accent10
        )
      }
      when (state.loading) {
        true -> {
          item {
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
              Column {
                HeightSpacer(value = 8.dp)
                CircularProgressIndicator(
                  color = AppTheme.colors.primaryContent,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }
        }
        else -> {
          when (state.isThreadEmpty) {
            true -> {
              item {
                HeightSpacer(value = 8.dp)
                EmptyThread()
              }
            }
            else -> {
              items(state.thread) {
                StatusListItem(
                  status = it,
                  favouriteStatus = { viewModel.favoriteStatus(status.threadId) },
                  unfavouriteStatus = { viewModel.unfavoriteStatus(status.threadId) },
                  navigateToDetail = { },
                  navigateToMedia = { attachments, index ->
                    navigator.navigate(
                      StatusMediaScreenDestination(
                        attachments = attachments.toTypedArray(),
                        targetMediaIndex = index
                      )
                    )
                  },
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                if (it.isReplyEnd) HeightSpacer(12.dp)
              }
            }
          }
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.loadThread(status.threadId)
  }
}

@Composable
fun EmptyThread() {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_thread))
  val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = LottieConstants.IterateForever
  )
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(250.dp)
      )
      HeightSpacer(value = 12.dp)
      Text(
        text = "这条嘟文暂时还没有任何回复噢",
        color = AppTheme.colors.cardAction,
        fontSize = 18.sp
      )
    }
  }
}
