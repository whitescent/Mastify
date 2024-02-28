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
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DatabaseTest {

  private lateinit var accountDao: TestAccountDao
  private lateinit var timelineDao: TestTimelineDao
  private lateinit var db: TestAppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, TestAppDatabase::class.java).build()
    accountDao = db.accountDao()
    timelineDao = db.timelineDao()
  }

  @After
  @Throws(IOException::class)
  fun closeDb() {
    db.close()
  }

  @Test
  fun `test changing the active account within the coroutine scope`() = runTest {
    suspend fun cleanAndReinsert() {
      db.withTransaction {
        val activeAccountId = accountDao.getActiveAccount()!!.id
        val timeline = timelineDao.getList(activeAccountId).toMutableList()
        timelineDao.clearAll(activeAccountId)
        timeline.add(TimelineItem(0, activeAccountId, "test"))
        timelineDao.insertOrUpdate(timeline)
      }
    }

    val account1 = AccountItem(1, "m.cmx.im", "lucky", true)
    val account2 = AccountItem(2, "google.wtf", "a", false)

    accountDao.insert(account1)
    accountDao.insert(account2)

    timelineDao.insertOrUpdate((1..30).toList().map { TimelineItem(0, 1, "a") })
    timelineDao.insertOrUpdate((1..10).toList().map { TimelineItem(0, 2, "B") })

    assertEquals(30, timelineDao.getList(1).size)
    assertEquals(10, timelineDao.getList(2).size)

    assertEquals(1, accountDao.getActiveAccount()!!.id)
    accountDao.setActiveAccount(2)
    assertEquals(2, accountDao.getActiveAccount()!!.id)

    var activeCode = 2L
    for (index in 1..2) {
      activeCode = if (activeCode == 2L) 1L else 2L
      accountDao.setActiveAccount(activeCode)
      runBlocking {
        cleanAndReinsert()
      }
    }
    assertEquals(31, timelineDao.getList(1).size)
    assertEquals(11, timelineDao.getList(2).size)
  }
}

@Entity
private data class AccountItem(
  @PrimaryKey(autoGenerate = true) val id: Long,
  val domain: String,
  val username: String,
  val isActive: Boolean
)

@Entity(
  foreignKeys = [
    ForeignKey(
      entity = AccountItem::class,
      parentColumns = ["id"],
      childColumns = ["timelineAccountId"]
    )
  ],
)
private data class TimelineItem(
  @PrimaryKey(autoGenerate = true) val id: Int,
  val timelineAccountId: Long,
  val content: String
)

@Database(
  version = 1,
  entities = [AccountItem::class, TimelineItem::class]
)
private abstract class TestAppDatabase() : RoomDatabase() {
  abstract fun accountDao(): TestAccountDao
  abstract fun timelineDao(): TestTimelineDao
}

@Dao
private interface TestAccountDao {

  @Query("SELECT * FROM AccountItem WHERE id = :id LIMIT 1")
  suspend fun getAccountById(id: Long): AccountItem

  @Upsert
  suspend fun insert(vararg accountItem: AccountItem)

  @Insert(onConflict = REPLACE)
  suspend fun insertOrUpdate(account: AccountItem)

  @Query("SELECT * FROM AccountItem WHERE isActive = 1 LIMIT 1")
  suspend fun getActiveAccount(): AccountItem?

  @Query("UPDATE AccountItem SET isActive = :isActive WHERE id = :accountId")
  suspend fun setAccountActiveState(accountId: Long, isActive: Boolean)

  @Transaction
  suspend fun setActiveAccount(accountId: Long) {
    deactivateCurrentlyActiveAccount()
    setAccountActiveState(accountId, true)
  }

  private suspend fun deactivateCurrentlyActiveAccount() {
    getActiveAccount()?.id?.let { currentAccountId ->
      setAccountActiveState(currentAccountId, false)
    }
  }
}

@Dao
private interface TestTimelineDao {

  @Query("DELETE FROM timelineitem WHERE timelineAccountId = :accountId")
  suspend fun clearAll(accountId: Long)

  @Insert(onConflict = REPLACE)
  suspend fun insertOrUpdate(timelineEntity: List<TimelineItem>)

  @Query("SELECT * FROM timelineitem WHERE timelineAccountId = :accountId")
  suspend fun getList(accountId: Long): List<TimelineItem>
}
