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
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.status.StatusSnackbarType
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.Bookmark
import com.github.whitescent.mastify.utils.StatusAction.Favorite
import com.github.whitescent.mastify.utils.StatusAction.Reblog
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
      is Favorite -> {
        if (action.favorite) api.favouriteStatus(action.id) else api.unfavouriteStatus(action.id)
      }
      is Reblog -> {
        if (action.reblog) api.reblogStatus(action.id) else api.unreblogStatus(action.id)
      }
      is Bookmark -> {
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

  companion object {
    /**
     * Update the status of the action in the status list, for example,
     * if the user likes a status, we need to update the status and number of FAVs
     */
    fun updateStatusListActions(
      list: List<Status>,
      action: StatusAction,
      statusId: String
    ): List<Status> {
      val newList = list.toMutableList()
      val index = newList.indexOfFirst { it.id == statusId }
      if (index != -1) {
        var status = newList[index]

        val favorite = getStatusFavorite(status, action)
        val favouritesCount = getStatusFavoriteCount(status, action)

        val reblog = getStatusReblog(status, action)
        val reblogsCount = getStatusReblogCount(status, action)

        val bookmark = getStatusBookmark(status, action)

        when (status.reblog == null) {
          true -> {
            status = status.copy(
              favorited = favorite,
              favouritesCount = favouritesCount,
              reblogged = reblog,
              reblogsCount = reblogsCount,
              bookmarked = bookmark
            )
          }
          else -> {
            status = status.copy(
              reblog = status.reblog!!.copy(
                favorited = favorite,
                favouritesCount = favouritesCount,
                reblogged = reblog,
                reblogsCount = reblogsCount,
                bookmarked = bookmark
              )
            )
          }
        }
        newList[index] = status
        return newList
      } else return list
    }

    fun updateSingleStatusActions(currentStatus: Status, action: StatusAction): Status {
      var newStatus = currentStatus

      val favorite = getStatusFavorite(currentStatus, action)
      val favouritesCount = getStatusFavoriteCount(currentStatus, action)

      val reblog = getStatusReblog(currentStatus, action)
      val reblogsCount = getStatusReblogCount(currentStatus, action)

      val bookmark = getStatusBookmark(currentStatus, action)

      when (newStatus.reblog == null) {
        true -> {
          newStatus = newStatus.copy(
            favorited = favorite,
            favouritesCount = favouritesCount,
            reblogged = reblog,
            reblogsCount = reblogsCount,
            bookmarked = bookmark
          )
        }
        else -> {
          newStatus = newStatus.copy(
            reblog = newStatus.reblog!!.copy(
              favorited = favorite,
              favouritesCount = favouritesCount,
              reblogged = reblog,
              reblogsCount = reblogsCount,
              bookmarked = bookmark
            )
          )
        }
      }
      return newStatus
    }

    private fun getStatusFavorite(status: Status, action: StatusAction): Boolean {
      // Check whether the status contains reblog,
      // and if so, we need to determine the status of the reblog
      return when (status.reblog == null) {
        true -> (action as? Favorite)?.favorite ?: status.favorited
        else -> (action as? Favorite)?.favorite ?: status.reblog.favorited
      }
    }

    private fun getStatusFavoriteCount(status: Status, action: StatusAction): Int {
      return when (status.reblog == null) {
        true -> (action as? Favorite)?.let { state ->
          status.favouritesCount + if (state.favorite) 1 else -1
        } ?: status.favouritesCount
        else -> (action as? Favorite)?.let { state ->
          status.reblog.favouritesCount + if (state.favorite) 1 else -1
        } ?: status.reblog.favouritesCount
      }
    }

    private fun getStatusReblog(status: Status, action: StatusAction): Boolean {
      return when (status.reblog == null) {
        true -> (action as? Reblog)?.reblog ?: status.reblogged
        else -> (action as? Reblog)?.reblog ?: status.reblog.reblogged
      }
    }

    private fun getStatusReblogCount(status: Status, action: StatusAction): Int {
      return when (status.reblog == null) {
        true -> (action as? Reblog)?.let { state ->
          status.reblogsCount + if (state.reblog) 1 else -1
        } ?: status.reblogsCount
        else -> (action as? Reblog)?.let { state ->
          status.reblog.reblogsCount + if (state.reblog) 1 else -1
        } ?: status.reblog.reblogsCount
      }
    }

    private fun getStatusBookmark(status: Status, action: StatusAction): Boolean {
      return when (status.reblog == null) {
        true -> (action as? Bookmark)?.bookmark ?: status.bookmarked
        else -> (action as? Bookmark)?.bookmark ?: status.reblog.bookmarked
      }
    }
  }
}
