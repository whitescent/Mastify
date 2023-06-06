package com.github.whitescent.mastify.database.dao

import androidx.paging.PagingSource
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

  @Query("SELECT * FROM timelineentity ORDER BY LENGTH(id) DESC, id DESC")
  fun getStatuses(): PagingSource<Int, Status>

  @Query("SELECT id FROM timelineentity ORDER BY LENGTH(id) DESC, id DESC LIMIT 1")
  suspend fun getTopId(): String?

  @Query(
    """
    DELETE FROM timelineentity WHERE
    (LENGTH(id) < LENGTH(:maxId) OR LENGTH(id) == LENGTH(:maxId) AND id <= :maxId)
    AND
    (LENGTH(id) > LENGTH(:minId) OR LENGTH(id) == LENGTH(:minId) AND id >= :minId)
    """
  )
  suspend fun deleteRange(minId: String, maxId: String)

  @Query("DELETE FROM timelineentity")
  suspend fun clearAll()

}
