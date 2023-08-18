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
        createdAt = newAccount.createdAt
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

  /**
   * changes the active account
   * @param accountId the database id of the new active account
   */
  fun setActiveAccount(accountId: Long) {
    val newActiveAccount = accounts.find { (id) ->
      id == accountId
    } ?: return // invalid accountId passed, do nothing

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
    if (account.id != 0L) {
      accountDao.insertOrReplace(account)
    }
  }
}
