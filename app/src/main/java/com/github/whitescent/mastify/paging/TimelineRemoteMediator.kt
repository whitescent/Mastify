package com.github.whitescent.mastify.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.database.MastifyDatabase
import com.github.whitescent.mastify.network.model.response.account.Status
import okio.IOException
import retrofit2.HttpException

@OptIn(ExperimentalPagingApi::class)
class TimelineRemoteMediator (
  private val db: MastifyDatabase,
  private val api: ApiRepository,
  preferenceRepository: PreferenceRepository
) : RemoteMediator<Int, Status>() {

  private val timelineDao = db.timelineDao()
  private val account = preferenceRepository.account!!

  private var initialRefresh = false

  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, Status>
  ): MediatorResult {

    return try {

      val topId = timelineDao.getTopId()

      if (!initialRefresh && loadType == LoadType.REFRESH) {
        topId?.let {
          val statuses = api.getHomeTimeline(
            maxId = it,
            instanceName = account.instanceName,
            token = account.accessToken
          )
          db.withTransaction {
            replaceStatusRange(statuses)
          }
        }
        initialRefresh = true
      }
      val statuses = when (loadType) {
        LoadType.REFRESH -> {
          api.getHomeTimeline(
            instanceName = account.instanceName,
            token = account.accessToken
          )
        }
        LoadType.PREPEND -> {
          return MediatorResult.Success(endOfPaginationReached = true)
        }
        LoadType.APPEND -> {
          val maxId = state.pages.findLast { it.data.isNotEmpty() }?.data?.lastOrNull()?.id
          api.getHomeTimeline(
            maxId = maxId,
            instanceName = account.instanceName,
            token = account.accessToken
          )
        }
      }
      val endOfPaginationReached = statuses.isEmpty()

      db.withTransaction {
        replaceStatusRange(statuses)
      }

      return MediatorResult.Success(endOfPaginationReached)

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
