package com.github.whitescent.mastify.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.whitescent.mastify.database.dao.AccountDao
import com.github.whitescent.mastify.database.dao.TimelineDao
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.database.util.StatusConverter

@Database(
  entities = [
    TimelineEntity::class,
    AccountEntity::class
  ],
  version = 1
)
@TypeConverters(
  StatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun timelineDao(): TimelineDao
  abstract fun accountDao(): AccountDao
}
