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

package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.getOrThrow
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.dao.AccountDao
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.mapper.toEntity
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
  db: AppDatabase,
  private val api: MastodonApi
) {

  private val accountDao: AccountDao = db.accountDao()

  suspend fun setActiveAccount(accountId: Long) {
    accountDao.setActiveAccount(accountId)
  }

  suspend fun updateActiveAccount(account: AccountEntity) {
    accountDao.insertOrUpdate(account)
  }

  suspend fun addAccount(newAccount: AccountEntity) {
    accountDao.addAccount(newAccount)
  }

  suspend fun fetchAccountVerifyCredentials(domain: String, token: String): Flow<Account> = flow {
    emit(api.accountVerifyCredentials(domain, "Bearer $token").getOrThrow())
  }

  suspend fun fetchActiveAccountAndSaveToDatabase() {
    val activeAccount = accountDao.getActiveAccount()!!
    fetchAccountVerifyCredentials(activeAccount.domain, activeAccount.accessToken)
      .catch {
        it.printStackTrace()
      }
      .collect {
        updateActiveAccount(
          it.toEntity(
            accessToken = activeAccount.accessToken,
            clientId = activeAccount.clientId,
            clientSecret = activeAccount.clientSecret,
            isActive = activeAccount.isActive,
            accountId = activeAccount.accountId,
            id = activeAccount.id,
            firstVisibleItemIndex = activeAccount.firstVisibleItemIndex,
            offset = activeAccount.offset
          )
        )
      }
  }
}
