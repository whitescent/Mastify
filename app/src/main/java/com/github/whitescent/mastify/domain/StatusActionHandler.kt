/*
 * Copyright 2023 WhiteScent
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

package com.github.whitescent.mastify.domain

import android.content.ClipData
import android.content.ClipboardManager
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
    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
