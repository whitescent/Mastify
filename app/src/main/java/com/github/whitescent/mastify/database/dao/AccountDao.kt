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

package com.github.whitescent.mastify.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.github.whitescent.mastify.database.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

  @Upsert
  suspend fun insertOrUpdate(account: AccountEntity)

  @Insert(entity = AccountEntity::class)
  suspend fun insert(account: AccountEntity)

  @Query("SELECT * FROM ACCOUNTENTITY WHERE id = :id")
  suspend fun getAccount(id: Long): AccountEntity

  @Query("SELECT * FROM ACCOUNTENTITY")
  fun getAccountListFlow(): Flow<List<AccountEntity>>

  @Query("SELECT * FROM ACCOUNTENTITY")
  suspend fun getAccountList(): List<AccountEntity>

  @Query("SELECT * FROM ACCOUNTENTITY WHERE isActive = 1 LIMIT 1")
  fun getActiveAccountFlow(): Flow<AccountEntity?>

  @Query("SELECT * FROM ACCOUNTENTITY WHERE isActive = 1 LIMIT 1")
  suspend fun getActiveAccount(): AccountEntity?

  @Query("SELECT * FROM ACCOUNTENTITY WHERE accountId = :accountId")
  suspend fun getAccountByAccountId(accountId: String): AccountEntity?

  @Query("SELECT * FROM ACCOUNTENTITY WHERE accountId = :instanceAccountId AND domain = :domain")
  suspend fun getAccountByInstanceInfo(instanceAccountId: String, domain: String): AccountEntity?

  @Delete
  suspend fun delete(account: AccountEntity)

  @Query("SELECT * FROM AccountEntity ORDER BY id ASC")
  suspend fun loadAll(): List<AccountEntity>

  @Query("UPDATE AccountEntity SET isActive = :isActive WHERE id = :accountId")
  suspend fun setAccountActiveState(accountId: Long, isActive: Boolean)

  @Transaction
  suspend fun setActiveAccount(accountId: Long) {
    deactivateCurrentlyActiveAccount()
    setAccountActiveState(accountId, true)
  }

  @Transaction
  suspend fun addAccount(account: AccountEntity) {
    deactivateCurrentlyActiveAccount()
    val existingAccount =
      getAccountByInstanceInfo(instanceAccountId = account.accountId, domain = account.domain)

    if (existingAccount != null) {
      insertOrUpdate(account.copy(id = existingAccount.id, isActive = true))
    } else {
      insertOrUpdate(account.copy(isActive = true))
    }
  }

  private suspend fun deactivateCurrentlyActiveAccount() {
    getActiveAccount()?.id?.let { currentAccountId ->
      setAccountActiveState(currentAccountId, false)
    }
  }
}
