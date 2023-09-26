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
import androidx.room.Query
import androidx.room.Upsert
import com.github.whitescent.mastify.database.model.AccountEntity

@Dao
interface AccountDao {

  @Upsert(entity = AccountEntity::class)
  fun insertOrReplace(account: AccountEntity)

  @Query("SELECT * FROM ACCOUNTENTITY WHERE id = :id")
  fun getAccount(id: Long): AccountEntity

  @Delete
  fun delete(account: AccountEntity)

  @Query("SELECT * FROM AccountEntity ORDER BY id ASC")
  fun loadAll(): List<AccountEntity>
}
