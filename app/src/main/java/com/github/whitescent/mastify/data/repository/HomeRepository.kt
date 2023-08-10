package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.network.MastodonApi
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
}
