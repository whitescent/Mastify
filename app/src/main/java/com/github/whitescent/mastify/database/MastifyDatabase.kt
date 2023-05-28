package com.github.whitescent.mastify.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.whitescent.mastify.database.dao.TimelineDao
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.database.util.StatusConverter

@Database(
  entities = [TimelineEntity::class],
  version = 1
)
@TypeConverters(
  StatusConverter::class
)
abstract class MastifyDatabase : RoomDatabase() {
  abstract fun timelineDao(): TimelineDao
}
