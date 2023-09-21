package com.github.whitescent.mastify.domain

import android.content.ClipData
import android.content.Context
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.ui.component.status.StatusSnackbarType
import com.github.whitescent.mastify.utils.StatusAction
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@ViewModelScoped
class StatusActionHandler(private val api: MastodonApi) {

  private val snackBarChanel = Channel<StatusSnackbarType>(Channel.BUFFERED)
  val snackBarFlow = snackBarChanel.receiveAsFlow()

  suspend fun onStatusAction(action: StatusAction, context: Context) {
    val clipManager =
      context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    when (action) {
      is StatusAction.Favorite -> {
        if (action.favorite) api.favouriteStatus(action.id) else api.unfavouriteStatus(action.id)
      }
      is StatusAction.Reblog -> {
        if (action.reblog) api.reblogStatus(action.id) else api.unreblogStatus(action.id)
      }
      is StatusAction.Bookmark -> {
        if (action.bookmark) {
          api.bookmarkStatus(action.id)
          snackBarChanel.send(StatusSnackbarType.Bookmark)
        } else api.unbookmarkStatus(action.id)
      }
      is StatusAction.CopyText -> {
        clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.text))
        snackBarChanel.send(StatusSnackbarType.Text)
      }
      is StatusAction.CopyLink -> {
        clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.link))
        snackBarChanel.send(StatusSnackbarType.Link)
      }
      is StatusAction.Mute -> Unit // TODO
      is StatusAction.Block -> Unit
      is StatusAction.Report -> Unit
    }
  }

  suspend fun onStatusLoadError() = snackBarChanel.send(StatusSnackbarType.Error)
}
