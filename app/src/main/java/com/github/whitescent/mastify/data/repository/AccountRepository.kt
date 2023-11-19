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

package com.github.whitescent.mastify.data.repository

import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.dao.AccountDao
import com.github.whitescent.mastify.database.model.AccountEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(db: AppDatabase) {

  private val accountDao: AccountDao = db.accountDao()

  // suspend fun initAccount() {
  //   accountList.value = accountDao.loadAll().toMutableList()
  //   activeAccount.value = accountList.value.find { it.isActive }
  // }

  suspend fun setActiveAccount(accountId: Long) {
    var currentActiveAccount = accountDao.getActiveAccount()
    currentActiveAccount?.let {
      currentActiveAccount = it.copy(isActive = false)
      accountDao.insertOrUpdate(currentActiveAccount!!)
      val newActiveAccount = accountDao.getAccount(accountId).copy(isActive = true)
      accountDao.insertOrUpdate(newActiveAccount)
    }
  }

  suspend fun updateActiveAccount(account: AccountEntity) {
    accountDao.insertOrUpdate(account)
  }

  suspend fun addAccount(newAccount: AccountEntity) {
    var activeAccount = accountDao.getActiveAccount()
    activeAccount?.let {
      activeAccount = it.copy(isActive = false)
      accountDao.insertOrUpdate(activeAccount!!)
    }
    // check if this is a relogin with an existing account,
    // if yes update it, otherwise create a new one
    val accounts = accountDao.getAccountList().toMutableList()
    val existingAccountIndex = accounts.indexOfFirst { account ->
      newAccount.domain == account.domain && newAccount.accountId == account.accountId
    }
    if (existingAccountIndex != -1)
      accountDao.insertOrUpdate(newAccount.copy(id = accounts[existingAccountIndex].id))
    else accountDao.insert(newAccount)
  }
}
