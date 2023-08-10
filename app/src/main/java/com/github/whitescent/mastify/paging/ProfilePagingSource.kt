package com.github.whitescent.mastify.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProfilePagingSource @Inject constructor(
  private val api: MastodonApi,
  private val viewModel: ProfileViewModel
) : PagingSource<String, StatusUiData>() {

  private var nextPageId: String? = null

  override fun getRefreshKey(state: PagingState<String, StatusUiData>): String? {
    return if (nextPageId == null) null else nextPageId
  }

  override suspend fun load(params: LoadParams<String>): LoadResult<String, StatusUiData> {
    return try {
      val data = api.accountStatuses(
        accountId = viewModel.uiState.account.id,
        maxId = if (nextPageId != null) nextPageId else null,
        excludeReplies = true
      ).body()!!.toUiData()
      LoadResult.Page(
        data = data,
        prevKey = nextPageId,
        nextKey = if (data.isEmpty()) null else data.last().id
      ).also {
        it.nextKey?.let { id -> nextPageId = id }
      }
    } catch (exception: IOException) {
      exception.printStackTrace()
      return LoadResult.Error(exception)
    } catch (exception: HttpException) {
      exception.printStackTrace()
      return LoadResult.Error(exception)
    }
  }
}
