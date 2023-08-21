package com.github.whitescent.mastify.screen.other

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.R
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.extensions.insertString
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.EmojiSheet
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.ReplyTextField
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.component.status.StatusDetailCard
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.statusComment
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.StatusDetailTransitions
import com.github.whitescent.mastify.viewModel.StatusDetailViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

data class StatusDetailNavArgs(
  val avatar: String,
  val status: Status
)

@OptIn(ExperimentalMaterial3Api::class)
@AppNavGraph
@Destination(
  style = StatusDetailTransitions::class,
  navArgsDelegate = StatusDetailNavArgs::class
)
@Composable
fun StatusDetail(
  navigator: DestinationsNavigator,
  viewModel: StatusDetailViewModel = hiltViewModel()
) {
  val lazyState = rememberLazyListState()
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  val state = viewModel.uiState
  val replyText = viewModel.replyField

  val status = viewModel.navArgs.status.toUiData()
  val avatar = viewModel.navArgs.avatar
  val threadInReply = status.reblog?.isInReplyTo ?: status.isInReplyTo

  var openSheet by remember { mutableStateOf(false) }

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
    AppHorizontalDivider()
    when (threadInReply) {
      true -> {
        StatusDetailInReply(
          status = status,
          lazyState = lazyState,
          ancestors = state.ancestors.toImmutableList(),
          descendants = state.descendants.toImmutableList(),
          loading = state.loading,
          favouriteStatus = viewModel::favoriteStatus,
          unfavouriteStatus = viewModel::unfavoriteStatus,
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
          navigateToProfile = {
            navigator.navigate(ProfileDestination(it))
          },
          modifier = Modifier.weight(1f)
        )
      }
      else -> {
        StatusDetailContent(
          status = status,
          lazyState = lazyState,
          descendants = state.descendants.toImmutableList(),
          loading = state.loading,
          favouriteStatus = viewModel::favoriteStatus,
          unfavouriteStatus = viewModel::unfavoriteStatus,
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
          navigateToProfile = { navigator.navigate(ProfileDestination(it)) },
          modifier = Modifier.weight(1f)
        )
      }
    }
    ReplyTextField(
      targetAccount = viewModel.navArgs.status.account,
      fieldValue = replyText,
      postState = state.postState,
      onValueChange = viewModel::updateText,
      replyToStatus = viewModel::replyToStatus,
      openEmojiPicker = { openSheet = true }
    )
  }
  if (openSheet) {
    EmojiSheet(
      sheetState = sheetState,
      emojis = state.instanceEmojis,
      onDismissRequest = { openSheet = false },
      onSelectEmoji = {
        viewModel.updateText(
          text = viewModel.replyField.copy(
            text = viewModel.replyField.text.insertString(it, viewModel.replyField.selection.start),
            selection = TextRange(viewModel.replyField.selection.start + it.length + 1)
          )
        )
        scope.launch {
          sheetState.hide()
        }.invokeOnCompletion { openSheet = false }
      }
    )
  }
  BackHandler(sheetState.isVisible) { openSheet = false }
}

@Composable
fun StatusDetailContent(
  status: StatusUiData,
  lazyState: LazyListState,
  descendants: ImmutableList<StatusUiData>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  favouriteStatus: (String) -> Unit,
  unfavouriteStatus: (String) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .drawVerticalScrollbar(lazyState),
    state = lazyState
  ) {
    item {
      StatusDetailCard(
        status = status,
        favouriteStatus = { favouriteStatus(status.actionableId) },
        unfavouriteStatus = { unfavouriteStatus(status.actionableId) },
        navigateToDetail = { navigateToDetail(status.actionable) },
        navigateToMedia = navigateToMedia,
        navigateToProfile = navigateToProfile
      )
    }
    item {
      AppHorizontalDivider()
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
        statusComment(
          descendants = descendants,
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
          navigateToDetail = navigateToDetail,
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile
        )
      }
    }
  }
}

@Composable
fun StatusDetailInReply(
  status: StatusUiData,
  lazyState: LazyListState,
  ancestors: ImmutableList<StatusUiData>,
  descendants: ImmutableList<StatusUiData>,
  loading: Boolean,
  modifier: Modifier = Modifier,
  favouriteStatus: (String) -> Unit,
  unfavouriteStatus: (String) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (List<Attachment>, Int) -> Unit,
) {
  LazyColumn(modifier = modifier, state = lazyState) {
    itemsIndexed(
      items = ancestors + status,
      key = { _, item -> item.id }
    ) { index, repliedStatus ->
      if (repliedStatus == status) {
        StatusDetailCard(
          status = status,
          favouriteStatus = { favouriteStatus(status.actionableId) },
          unfavouriteStatus = { unfavouriteStatus(status.actionableId) },
          navigateToDetail = { navigateToDetail(status.actionable) },
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile,
          inReply = true
        )
      } else {
        StatusListItem(
          status = repliedStatus,
          replyChainType = if (index == 0) Start else Continue,
          hasUnloadedParent = false,
          favouriteStatus = { favouriteStatus(repliedStatus.actionableId) },
          unfavouriteStatus = { unfavouriteStatus(repliedStatus.actionableId) },
          navigateToDetail = { navigateToDetail(repliedStatus.actionable) },
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile
        )
      }
    }
    item {
      AppHorizontalDivider()
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
        statusComment(
          descendants = descendants,
          favouriteStatus = favouriteStatus,
          unfavouriteStatus = unfavouriteStatus,
          navigateToDetail = navigateToDetail,
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile
        )
      }
    }
  }
}
