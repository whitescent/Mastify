package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
  private val api: MastodonApi,
  private val accountRepository: AccountRepository
) {

  val activeAccount get() = accountRepository.activeAccount!!

  suspend fun updateAccountInfo() {
    api.accountVerifyCredentials(
      domain = activeAccount.domain,
      auth = "Bearer ${activeAccount.accessToken}"
    )
      .fold(
        {
          accountRepository.updateActiveAccount(it)
        },
        {
          it.printStackTrace()
        }
      )
  }

  fun timelineListHandler(oldItems: List<Status>, newItems: List<Status>): List<Status> {
    when (newItems.isEmpty()) {
      true -> return emptyList()
      else -> {
        if (oldItems.size < timelineFetchNumber || newItems.size < timelineFetchNumber) return newItems
        else {
          val lastStatusOfNewItems = newItems.last()
          if (oldItems.any { it.id == lastStatusOfNewItems.id }) {
            val indexInSavedList = oldItems.indexOfFirst {
              it.id == lastStatusOfNewItems.id
            } + 1
            val statusListAfterFetchNumber =
              oldItems.subList(indexInSavedList, oldItems.size)
            val statusListWithUnloaded =
              (oldItems - statusListAfterFetchNumber.toSet()).filter { it.hasUnloadedStatus }
            val statusListBeforeFetchNumber = newItems.toMutableList().apply {
              replaceAll { new ->
                statusListWithUnloaded.find { saved -> saved.id == new.id } ?: new
              }
            }
            return statusListBeforeFetchNumber + statusListAfterFetchNumber
          } else {
            // If the last status returned by the API cannot be found in the saved status list,
            // This means that the number of statuses in the user's timeline exceeds
            // the number of statuses in a single API request,
            // and we need to display 'Load More' button
            val newStatusList = newItems.toMutableList()
            newStatusList[newStatusList.lastIndex] =
              newStatusList[newStatusList.lastIndex].copy(hasUnloadedStatus = true)
            newItems.forEach {
              if (oldItems.any { saved -> saved.id == it.id }) {
                val removeIndex = oldItems.indexOfFirst { it.hasUnloadedStatus }.let {
                  if (it == -1) 0 else it + 1
                }
                return newStatusList + oldItems.subList(removeIndex, oldItems.size)
              }
            }
            return newStatusList + oldItems
          }
        }
      }
    }
  }

  companion object {
    const val timelineFetchNumber = 20
  }
}
