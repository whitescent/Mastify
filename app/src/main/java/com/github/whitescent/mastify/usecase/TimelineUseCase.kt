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

package com.github.whitescent.mastify.usecase

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import at.connyduck.calladapter.networkresult.NetworkResult
import at.connyduck.calladapter.networkresult.getOrThrow
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Poll
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.status.StatusSnackbarType
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.Bookmark
import com.github.whitescent.mastify.utils.StatusAction.Favorite
import com.github.whitescent.mastify.utils.StatusAction.Reblog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TimelineUseCase @Inject constructor(
  private val api: MastodonApi,
  @ApplicationContext private val context: Context
) {

  private val snackBarChanel = Channel<StatusSnackbarType>(Channel.BUFFERED)
  val snackBarFlow = snackBarChanel.receiveAsFlow()

  suspend fun onStatusAction(action: StatusAction): NetworkResult<Status>? {
    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return when (action) {
      is Favorite -> {
        if (action.favorite) api.favouriteStatus(action.id) else api.unfavouriteStatus(action.id)
      }
      is Reblog -> {
        if (action.reblog) api.reblogStatus(action.id) else api.unreblogStatus(action.id)
      }
      is Bookmark -> {
        if (action.bookmark) {
          snackBarChanel.send(StatusSnackbarType.Bookmark)
          api.bookmarkStatus(action.id)
        } else api.unbookmarkStatus(action.id)
      }
      is StatusAction.VotePoll -> {
        try {
          val poll = api.voteInPoll(action.id, action.choices).getOrThrow()
          NetworkResult.success(action.originStatus.copy(poll = poll))
        } catch (e: Exception) {
          withContext(Dispatchers.Main) {
            Toast.makeText(
              context,
              context.getString(R.string.vote_failed_title, e.localizedMessage),
              Toast.LENGTH_SHORT
            ).show()
          }
          NetworkResult.failure(e)
        }
      }
      is StatusAction.CopyText -> {
        clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.text))
        snackBarChanel.send(StatusSnackbarType.Text)
        null
      }
      is StatusAction.CopyLink -> {
        clipManager.setPrimaryClip(ClipData.newPlainText("PLAIN_TEXT_LABEL", action.link))
        snackBarChanel.send(StatusSnackbarType.Link)
        null
      }
      StatusAction.Mute -> null // TODO
      StatusAction.Block -> null
      StatusAction.Report -> null
    }
  }

  suspend fun onStatusLoadError(throwable: Throwable?) =
    snackBarChanel.send(StatusSnackbarType.Error(throwable?.localizedMessage))

  companion object {
    /**
     * Update the status of the action in the status list, for example,
     * if the user likes a status, we need to update the status and number of FAVs
     * This function doesn't make an API request, it just manually modifies the relevant data
     * to make sure that onStatusAction is called after using this function
     */
    fun updateStatusListActions(
      list: List<StatusUiData>,
      action: StatusAction,
      statusId: String
    ): List<StatusUiData> {
      val newList = list.toMutableList()
      val index = newList.indexOfFirst { it.actionableId == statusId }
      if (index != -1) {
        var status = newList[index]

        val favorite = getStatusFavorite(status, action)
        val favouritesCount = getStatusFavoriteCount(status, action)

        val reblog = getStatusReblog(status, action)
        val reblogsCount = getStatusReblogCount(status, action)

        val bookmark = getStatusBookmark(status, action)

        status = status.copy(
          favorited = favorite,
          favouritesCount = favouritesCount,
          reblogged = reblog,
          reblogsCount = reblogsCount,
          bookmarked = bookmark,
          actionable = status.actionable.copy(
            favorited = favorite,
            favouritesCount = favouritesCount,
            reblogged = reblog,
            reblogsCount = reblogsCount,
            bookmarked = bookmark
          )
        )

        newList[index] = status
        return newList
      } else return list
    }

    @JvmName("updateStatusListActionsStatus")
    fun updateStatusListActions(
      list: List<Status>,
      action: StatusAction,
      statusId: String
    ): List<Status> {
      val newList = list.toMutableList()
      newList.forEachIndexed { index, current ->
        if (current.actionableId == statusId) {
          var status = newList[index]

          val favorite = getStatusFavorite(status, action)
          val favouritesCount = getStatusFavoriteCount(status, action)

          val reblog = getStatusReblog(status, action)
          val reblogsCount = getStatusReblogCount(status, action)

          val bookmark = getStatusBookmark(status, action)

          status = status.copy(
            favorited = favorite,
            favouritesCount = favouritesCount,
            reblogged = reblog,
            reblogsCount = reblogsCount,
            bookmarked = bookmark,
            reblog = status.reblog?.copy(
              favorited = favorite,
              favouritesCount = favouritesCount,
              reblogged = reblog,
              reblogsCount = reblogsCount,
              bookmarked = bookmark
            )
          )
          newList[index] = status
        }
      }
      return newList
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

    fun updatePollOfStatus(status: Status, poll: Poll): Status {
      return when (status.reblog == null) {
        true -> status.copy(poll = poll)
        else -> status.copy(reblog = status.reblog.copy(poll = poll))
      }
    }

    fun updatePollOfStatusList(
      statusList: List<StatusUiData>,
      targetId: String,
      poll: Poll
    ): List<StatusUiData> {
      val targetStatusIndex = statusList.indexOfFirst { it.actionableId == targetId }
      if (targetStatusIndex == -1) return statusList
      val newList = statusList.toMutableList()
      newList[targetStatusIndex] = newList[targetStatusIndex].copy(
        poll = poll,
        actionable = newList[targetStatusIndex].actionable.copy(
          poll = poll
        )
      )
      return newList
    }

    @JvmName("updatePollOfStatusListStatus")
    fun updatePollOfStatusList(
      statusList: List<Status>,
      targetId: String,
      poll: Poll
    ): List<Status> {
      val targetStatusIndex = statusList.indexOfFirst { it.actionableId == targetId }
      if (targetStatusIndex == -1) return statusList
      val newList = statusList.toMutableList()
      newList[targetStatusIndex] = newList[targetStatusIndex].copy(poll = poll)
      return newList
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

    private fun getStatusFavorite(status: StatusUiData, action: StatusAction): Boolean {
      return (action as? Favorite)?.favorite ?: status.favorited
    }

    private fun getStatusFavoriteCount(status: StatusUiData, action: StatusAction): Int {
      return (action as? Favorite)?.let { state ->
        status.favouritesCount + if (state.favorite) 1 else -1
      } ?: status.favouritesCount
    }

    private fun getStatusReblog(status: StatusUiData, action: StatusAction): Boolean {
      return (action as? Reblog)?.reblog ?: status.reblogged
    }

    private fun getStatusReblogCount(status: StatusUiData, action: StatusAction): Int {
      return (action as? Reblog)?.let { state ->
        status.reblogsCount + if (state.reblog) 1 else -1
      } ?: status.reblogsCount
    }

    private fun getStatusBookmark(status: StatusUiData, action: StatusAction): Boolean {
      return (action as? Bookmark)?.bookmark ?: status.bookmarked
    }
  }
}
