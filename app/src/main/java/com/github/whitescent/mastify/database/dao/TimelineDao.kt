package com.github.whitescent.mastify.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.network.model.account.Status

@Dao
interface TimelineDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(vararg timelineEntity: TimelineEntity)

  @Query(
    """
      SELECT * FROM timelineentity WHERE timelineUserId = :accountId
      ORDER BY LENGTH(id) DESC, id DESC
    """
  )
  fun getStatuses(accountId: Long): List<Status>

  @Query(
    """
      SELECT id FROM timelineentity WHERE timelineUserId = :accountId
      ORDER BY LENGTH(id) DESC, id DESC LIMIT 1
    """
  )
  suspend fun getTopId(accountId: Long): String?

  @Query(
    """
    DELETE FROM timelineentity WHERE
    timelineUserId = :accountId
    AND
    (LENGTH(id) < LENGTH(:maxId) OR LENGTH(id) == LENGTH(:maxId) AND id <= :maxId)
    AND
    (LENGTH(id) > LENGTH(:minId) OR LENGTH(id) == LENGTH(:minId) AND id >= :minId)
    """
  )
  suspend fun deleteRange(accountId: Long, minId: String, maxId: String)

  @Query("DELETE FROM timelineentity WHERE timelineUserId = :accountId")
  suspend fun clearAll(accountId: Long)
}
