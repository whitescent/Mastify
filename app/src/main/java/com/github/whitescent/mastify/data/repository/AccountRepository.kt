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
    activeAccount = accounts.find { acc -> acc.isActive }
      ?: accounts.firstOrNull()?.also { acc -> acc.isActive = true }
  }

  fun addAccount(
    accessToken: String,
    domain: String,
    clientId: String,
    clientSecret: String,
    newAccount: Account
  ) {
    activeAccount?.let {
      it.isActive = false
      accountDao.insertOrReplace(it)
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
        accountId = newAccount.id
      ).also { accounts.add(it) }
    }
    activeAccount = newAccountEntity
    updateActiveAccount(newAccount)
  }

  /**
   * updates the current account with new information from the mastodon api
   * and saves it in the database
   * @param account the [Account] object returned from the api
   */
  fun updateActiveAccount(account: Account) {
    activeAccount?.let {
      it.accountId = account.id
      it.username = account.username
      it.displayName = account.displayName
      it.profilePictureUrl = account.avatar
      it.followingCount = account.followersCount
      it.followersCount = account.followersCount
      it.header = account.header
      it.statusesCount = account.statusesCount
      accountDao.insertOrReplace(it)
    }
  }

  /**
   * changes the active account
   * @param accountId the database id of the new active account
   */
  fun setActiveAccount(accountId: Long) {
    val newActiveAccount = accounts.find { (id) ->
      id == accountId
    } ?: return // invalid accountId passed, do nothing

    activeAccount?.let {
      it.isActive = false
      saveAccount(it)
    }

    activeAccount = newActiveAccount

    activeAccount?.let {
      it.isActive = true
      accountDao.insertOrReplace(it)
    }
  }

  /**
   * Saves an already known account to the database.
   * New accounts must be created with [addAccount]
   * @param account the account to save
   */
  fun saveAccount(account: AccountEntity) {
    if (account.id != 0L) {
      accountDao.insertOrReplace(account)
    }
  }
}
