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
import com.github.whitescent.mastify.network.model.account.Account
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(db: AppDatabase) {

  var activeAccount: AccountEntity? = null

  var accounts: MutableList<AccountEntity> = mutableListOf()
    private set

  private val accountDao: AccountDao = db.accountDao()

  init {
    accounts = accountDao.loadAll().toMutableList()
    (accounts.find { acc -> acc.isActive } ?: accounts.firstOrNull())
      ?.copy(isActive = true)
      ?.let { activeAccount = it }
  }

  fun addAccount(
    accessToken: String,
    domain: String,
    clientId: String,
    clientSecret: String,
    newAccount: Account
  ) {
    activeAccount?.let {
      activeAccount = it.copy(isActive = false)
      accountDao.insertOrReplace(activeAccount!!)
    }
    // check if this is a relogin with an existing account,
    // if yes update it, otherwise create a new one
    val existingAccountIndex = accounts.indexOfFirst { account ->
      domain == account.domain && newAccount.id == account.accountId
    }
    val newAccountEntity = if (existingAccountIndex != -1) {
      accounts[existingAccountIndex].copy(
        accessToken = accessToken,
        clientId = clientId,
        clientSecret = clientSecret,
        isActive = true
      ).also { accounts[existingAccountIndex] = it }
    } else {
      val maxAccountId = accounts.maxByOrNull { it.id }?.id ?: 0
      val newAccountId = maxAccountId + 1
      AccountEntity(
        id = newAccountId,
        domain = domain.lowercase(Locale.ROOT),
        accessToken = accessToken,
        clientId = clientId,
        clientSecret = clientSecret,
        isActive = true,
        accountId = newAccount.id,
        username = newAccount.username,
        displayName = newAccount.displayName,
        profilePictureUrl = newAccount.avatar,
        followingCount = newAccount.followingCount,
        followersCount = newAccount.followersCount,
        header = newAccount.header,
        statusesCount = newAccount.statusesCount,
        fields = newAccount.fields,
        note = newAccount.note,
        emojis = newAccount.emojis,
        createdAt = newAccount.createdAt,
        firstVisibleItemIndex = 0,
        offset = 0
      ).also { accounts.add(it) }
    }
    activeAccount = newAccountEntity
    accountDao.insertOrReplace(activeAccount!!)
  }

  /**
   * updates the current account with new information from the mastodon api
   * and saves it in the database
   * @param account the [Account] object returned from the api
   */
  fun updateActiveAccount(account: Account) {
    activeAccount?.copy(
      accountId = account.id,
      username = account.username,
      displayName = account.displayName,
      profilePictureUrl = account.avatar,
      followingCount = account.followingCount,
      followersCount = account.followersCount,
      header = account.header,
      statusesCount = account.statusesCount,
      fields = account.fields,
      note = account.note,
      createdAt = account.createdAt
    )?.let {
      activeAccount = it
      accountDao.insertOrReplace(it)
    }
  }

  fun updateActiveAccount(account: AccountEntity) {
    activeAccount = account
    accountDao.insertOrReplace(account)
  }

  /**
   * changes the active account
   * @param accountId the database id of the new active account
   */
  fun setActiveAccount(accountId: Long) {
    val newActiveAccount = accountDao.getAccount(accountId)
    activeAccount?.copy(isActive = false)?.let {
      activeAccount = it
      saveAccount(it)
    }

    activeAccount = newActiveAccount

    activeAccount?.copy(isActive = true)?.let {
      activeAccount = it
      accountDao.insertOrReplace(it)
    }
  }

  /**
   * Saves an already known account to the database.
   * New accounts must be created with [addAccount]
   * @param account the account to save
   */
  private fun saveAccount(account: AccountEntity) {
    if (account.id != 0L) accountDao.insertOrReplace(account)
  }
}
