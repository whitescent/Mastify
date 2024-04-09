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

package com.github.whitescent.mastify.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.network.model.status.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
  @Insert(onConflict = REPLACE)
  suspend fun insertOrUpdate(vararg timelineEntity: TimelineEntity)

  @Insert(onConflict = REPLACE)
  suspend fun insertOrUpdate(timelineEntity: List<TimelineEntity>)

  @Query(
    """
      SELECT * FROM timelineentity WHERE timelineUserId = :accountId
      ORDER BY LENGTH(id) DESC, id DESC
    """
  )
  @RewriteQueriesToDropUnusedColumns
  suspend fun getStatusList(accountId: Long): List<Status>

  @Query(
    """
      SELECT * FROM timelineentity WHERE timelineUserId = :accountId AND id = :statusId LIMIT 1
    """
  )
  @RewriteQueriesToDropUnusedColumns
  suspend fun getSingleStatusWithId(accountId: Long, statusId: String): Status?

  @Query(
    """
      SELECT * FROM timelineentity WHERE timelineUserId = :accountId
      ORDER BY LENGTH(id) DESC, id DESC
    """
  )
  @RewriteQueriesToDropUnusedColumns
  fun getStatusListWithFlow(accountId: Long): Flow<List<Status>>

  @Query(
    """
      SELECT id FROM timelineentity WHERE timelineUserId = :accountId
      ORDER BY LENGTH(id) DESC, id DESC LIMIT 1
    """
  )
  suspend fun getTopId(accountId: Long): String?

  @Query("DELETE FROM timelineentity WHERE timelineUserId = :accountId AND id = :timelineId")
  suspend fun clear(accountId: Long, timelineId: String)

  @Query("DELETE FROM timelineentity WHERE timelineUserId = :accountId")
  suspend fun clearAll(accountId: Long)

  @Query(
    """
    DELETE FROM timelineentity
    WHERE timelineUserId = :accountId AND id NOT IN (
        SELECT id FROM timelineentity
        ORDER BY LENGTH(id) DESC, id DESC
        LIMIT :range
    )
    """
  )
  suspend fun cleanupOldTimeline(accountId: Long, range: Int)

  @Transaction
  suspend fun cleanAndReinsert(timelineEntity: List<TimelineEntity>, id: Long) {
    clearAll(id)
    insertOrUpdate(timelineEntity)
  }
}
