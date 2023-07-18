package com.github.whitescent.mastify.screen.other

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.End
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.status.StatusDetailCard
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.StatusTransitions
import com.github.whitescent.mastify.viewModel.StatusDetailViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class StatusDetailNavArgs(
  val avatar: String,
  val status: Status
)

@AppNavGraph
@Destination(
  style = StatusTransitions::class,
  navArgsDelegate = StatusDetailNavArgs::class
)
@Composable
fun StatusDetail(
  navigator: DestinationsNavigator,
  viewModel: StatusDetailViewModel = hiltViewModel()
) {
  val lazyState = rememberLazyListState()
  val state = viewModel.uiState
  val replyText by viewModel.replyText.collectAsStateWithLifecycle()

  val status = Status.ViewData(viewModel.navArgs.status)
  val avatar = viewModel.navArgs.avatar
  val threadInReply = status.reblog?.isInReplyTo ?: status.isInReplyTo

  Column(Modifier.fillMaxSize()) {
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
        text = stringResource(id = R.string.home_title),
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.primaryContent,
      )
    }
    Divider()
    when (threadInReply) {
      true -> {
        StatusDetailInReply(
          status = status,
          lazyState = lazyState,
          ancestors = state.ancestors.toImmutableList(),
          descendants = state.descendants.toImmutableList(),
          loading = state.loading,
          favouriteStatus = { viewModel.favoriteStatus(status.actionableId) },
          unfavouriteStatus = { viewModel.unfavoriteStatus(status.actionableId) },
          navigateToDetail = {
            if (it.id != status.actionableId) {
              navigator.navigate(
                StatusDetailDestination(
                  avatar = avatar,
                  status = it
                )
              )
            }
          },
          navigateToMedia = { attachments, index ->
            navigator.navigate(
              StatusMediaScreenDestination(
                attachments = attachments.toTypedArray(),
                targetMediaIndex = index
              )
            )
          },
          modifier = Modifier.weight(1f)
        )
      }
      else -> {
        StatusDetailContent(
          status = status,
          descendants = state.descendants.toImmutableList(),
          loading = state.loading,
          favouriteStatus = { viewModel.favoriteStatus(status.actionableId) },
          unfavouriteStatus = { viewModel.unfavoriteStatus(status.actionableId) },
          navigateToDetail = {
            if (it.id != status.actionableId) {
              navigator.navigate(
                StatusDetailDestination(
                  avatar = avatar,
                  status = it
                )
              )
            }
          },
          navigateToMedia = { attachments, index ->
            navigator.navigate(
              StatusMediaScreenDestination(
                attachments = attachments.toTypedArray(),
                targetMediaIndex = index
              )
            )
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
    ReplyTextField(
      avatar = avatar,
      text = replyText,
      onValueChange = viewModel::updateText
    )
  }
}

@Composable
fun StatusDetailContent(
  status: Status.ViewData,
  descendants: ImmutableList<Status.ViewData>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  favouriteStatus: (String) -> Unit,
  unfavouriteStatus: (String) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  LazyColumn(modifier = modifier) {
    item {
      StatusDetailCard(
        status = status,
        favouriteStatus = { favouriteStatus(status.actionableId) },
        unfavouriteStatus = { unfavouriteStatus(status.actionableId) },
        navigateToDetail = { navigateToDetail(status.actionable) },
        navigateToMedia = navigateToMedia,
        contentTextStyle = TextStyle(
          fontSize = 16.sp,
          color = AppTheme.colors.primaryContent
        ),
      )
    }
    item {
      HeightSpacer(value = 8.dp)
    }
    when (loading) {
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
        when (descendants.isEmpty()) {
          true -> {
            item {
              Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
              ) {
                Box(Modifier.size(8.dp).background(Color.Gray, CircleShape))
              }
            }
          }
          else -> {
            items(descendants) {
              key(it.actionableId) {
                StatusListItem(
                  status = it,
                  favouriteStatus = { favouriteStatus(it.actionableId) },
                  unfavouriteStatus = { unfavouriteStatus(it.actionableId) },
                  navigateToDetail = { navigateToDetail(it.actionable) },
                  navigateToMedia = navigateToMedia,
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                )
                if (it.isReplyEnd) HeightSpacer(8.dp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatusDetailInReply(
  status: Status.ViewData,
  lazyState: LazyListState,
  ancestors: ImmutableList<Status.ViewData>,
  descendants: ImmutableList<Status.ViewData>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  favouriteStatus: (String) -> Unit,
  unfavouriteStatus: (String) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  val currentStatus = status.copy(
    replyChainType = End,
    hasUnloadedReplyStatus = false,
    hasMultiReplyStatus = false,
  )
  Crossfade(
    targetState = loading,
    modifier = modifier,
    animationSpec = tween(0)
  ) {
    when (it) {
      true -> {
        Column(Modifier.padding(horizontal = 12.dp)) {
          StatusListItem(
            status = currentStatus,
            favouriteStatus = { favouriteStatus(currentStatus.actionableId) },
            unfavouriteStatus = { unfavouriteStatus(currentStatus.actionableId) },
            navigateToDetail = { navigateToDetail(currentStatus.actionable) },
            navigateToMedia = navigateToMedia
          )
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
        LazyColumn(modifier = modifier, state = lazyState) {
          items(ancestors + currentStatus) { repliedStatus ->
            StatusListItem(
              status = repliedStatus,
              favouriteStatus = { favouriteStatus(repliedStatus.actionableId) },
              unfavouriteStatus = { unfavouriteStatus(repliedStatus.actionableId) },
              navigateToDetail = { navigateToDetail(repliedStatus.actionable) },
              navigateToMedia = navigateToMedia,
              modifier = Modifier.padding(horizontal = 12.dp)
            )
          }
          item {
            HeightSpacer(value = 8.dp)
          }
          when (descendants.isEmpty()) {
            true -> {
              item {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Box(Modifier.size(8.dp).background(Color.Gray, CircleShape))
                }
              }
            }
            else -> {
              items(descendants) { replyStatus ->
                key(replyStatus.actionableId) {
                  StatusListItem(
                    status = replyStatus,
                    favouriteStatus = { favouriteStatus(replyStatus.actionableId) },
                    unfavouriteStatus = { unfavouriteStatus(replyStatus.actionableId) },
                    navigateToDetail = { navigateToDetail(replyStatus.actionable) },
                    navigateToMedia = navigateToMedia,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                  )
                  if (replyStatus.isReplyEnd) HeightSpacer(8.dp)
                }
              }
            }
          }
        }
        LaunchedEffect(Unit) {
          lazyState.scrollToItem(ancestors.size)
        }
      }
    }
  }
}

@Composable
fun ReplyTextField(
  avatar: String,
  text: String,
  onValueChange: (String) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .shadow(24.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
  ) {
    CenterRow(Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 24.dp)) {
      CircleShapeAsyncImage(model = avatar, modifier = Modifier.size(48.dp))
      WidthSpacer(value = 6.dp)
      BasicTextField(
        value = text,
        onValueChange = onValueChange,
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFFF4F4F4), RoundedCornerShape(12.dp))
          .border(2.dp, Color(0xFFE6E6E6), RoundedCornerShape(12.dp))
          .height(48.dp),
        maxLines = 1,
        textStyle = TextStyle(color = AppTheme.colors.primaryContent, fontSize = 16.sp),
      ) {
        Box(Modifier.fillMaxSize().padding(12.dp), Alignment.CenterStart) {
          if (text.isEmpty()) {
            Text(
              text = stringResource(id = R.string.post_your_reply),
              color = Color(0xFFBABABA),
              style = TextStyle(color = AppTheme.colors.primaryContent, fontSize = 16.sp),
            )
          }
          it()
        }
      }
    }
  }
}
