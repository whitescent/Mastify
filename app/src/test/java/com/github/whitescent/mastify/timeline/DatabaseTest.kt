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

package com.github.whitescent.mastify.timeline

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DatabaseTest {

  private lateinit var accountDao: TestDao
  private lateinit var db: TestAppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, TestAppDatabase::class.java).build()
    accountDao = db.testDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  @Throws(Exception::class)
  fun `test account relogin`() = runTest {
    val account1 = AccountItem(0, "account1", "m.cmx.im", "lucky")
    accountDao.insert(account1)
    val account2 = AccountItem(0, "account1", "m.cmx.im", "luckyqwe")
    addAccount(account2)
    Assert.assertEquals(1, accountDao.getAll().size)
    Assert.assertEquals("luckyqwe", accountDao.getAll().first().username)
  }

  private fun addAccount(newAccount: AccountItem) = runTest {
    // check if this is a relogin with an existing account,
    // if yes update it, otherwise create a new one
    val accounts = accountDao.getAll().toMutableList()
    val existingAccountIndex = accounts.indexOfFirst { account ->
      newAccount.domain == account.domain && newAccount.accountId == account.accountId
    }
    if (existingAccountIndex != -1) accountDao.insertOrUpdate(newAccount.copy(id = accounts[existingAccountIndex].id))
    else accountDao.insert(newAccount)
  }
}

@Entity
private data class AccountItem(
  @PrimaryKey(autoGenerate = true) val id: Int,
  val accountId: String,
  val domain: String,
  val username: String
)

@Database(
  version = 1,
  entities = [AccountItem::class]
)
private abstract class TestAppDatabase() : RoomDatabase() {
  abstract fun testDao(): TestDao
}

@Dao
private interface TestDao {

  @Query("SELECT * FROM AccountItem")
  suspend fun getAll(): List<AccountItem>

  @Query("SELECT * FROM AccountItem WHERE accountId = :accountId")
  suspend fun getAccountByAccountId(accountId: String): AccountItem?

  @Upsert
  suspend fun insert(vararg accountItem: AccountItem)

  @Insert(onConflict = REPLACE)
  suspend fun insertOrUpdate(account: AccountItem)
}
