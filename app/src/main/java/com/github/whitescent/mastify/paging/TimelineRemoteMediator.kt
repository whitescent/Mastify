package com.github.whitescent.mastify.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.Status
import okio.IOException
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class TimelineRemoteMediator(
  accountRepository: AccountRepository,
  private val db: AppDatabase,
  private val api: MastodonApi
) : RemoteMediator<Int, Status>() {

  private val timelineDao = db.timelineDao()
  private val activeAccount = accountRepository.activeAccount!!

  private var initialRefresh = false

  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, Status>
  ): MediatorResult {

    if (!activeAccount.isLoggedIn()) {
      return MediatorResult.Success(endOfPaginationReached = true)
    }

    return try {

      val topId = timelineDao.getTopId()

      if (!initialRefresh && loadType == LoadType.REFRESH) {
        topId?.let {
          val statusResponse = api.homeTimeline(maxId = it)
          val statuses = statusResponse.body()
          if (statusResponse.isSuccessful && statuses != null) {
            db.withTransaction {
              replaceStatusRange(statuses)
            }
          }
        }
        initialRefresh = true
      }
      val statusResponse = when (loadType) {
        LoadType.REFRESH -> api.homeTimeline()
        LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
        LoadType.APPEND -> {
          val maxId = state.pages.findLast { it.data.isNotEmpty() }?.data?.lastOrNull()?.id
          api.homeTimeline(maxId = maxId)
        }
      }
      val statuses = statusResponse.body()
      if (!statusResponse.isSuccessful || statuses == null) {
        return MediatorResult.Error(HttpException(statusResponse))
      }
      db.withTransaction {
        replaceStatusRange(statuses)
      }
      return MediatorResult.Success(endOfPaginationReached = statuses.isEmpty())
    } catch (e: IOException) {
      MediatorResult.Error(e)
    } catch (e: HttpException) {
      MediatorResult.Error(e)
    }
  }

  private suspend fun replaceStatusRange(
    statuses: List<Status>
  ) {
    if (statuses.isNotEmpty()) {
      timelineDao.deleteRange(statuses.last().id, statuses.first().id)
    }
    for (status in statuses) {
      timelineDao.insert(status.toEntity())
    }
  }
}
