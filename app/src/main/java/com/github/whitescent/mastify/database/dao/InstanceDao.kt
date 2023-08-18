package com.github.whitescent.mastify.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Upsert
import com.github.whitescent.mastify.database.model.EmojisEntity
import com.github.whitescent.mastify.database.model.InstanceEntity
import com.github.whitescent.mastify.database.model.InstanceInfoEntity

@Dao
interface InstanceDao {

  @Upsert(entity = InstanceEntity::class)
  suspend fun upsert(instance: InstanceInfoEntity)

  @Upsert(entity = InstanceEntity::class)
  suspend fun upsert(emojis: EmojisEntity)

  @RewriteQueriesToDropUnusedColumns
  @Query("SELECT * FROM InstanceEntity WHERE instance = :instance LIMIT 1")
  suspend fun getInstanceInfo(instance: String): InstanceInfoEntity?

  @RewriteQueriesToDropUnusedColumns
  @Query("SELECT * FROM InstanceEntity WHERE instance = :instance LIMIT 1")
  suspend fun getEmojiInfo(instance: String): EmojisEntity?
}
