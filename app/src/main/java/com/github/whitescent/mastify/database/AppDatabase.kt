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

package com.github.whitescent.mastify.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.whitescent.mastify.database.dao.AccountDao
import com.github.whitescent.mastify.database.dao.InstanceDao
import com.github.whitescent.mastify.database.dao.TimelineDao
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.database.model.InstanceEntity
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.database.util.Converters

@Database(
  entities = [
    TimelineEntity::class,
    AccountEntity::class,
    InstanceEntity::class
  ],
  version = 5,
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
    AutoMigration(from = 2, to = 3),
    AutoMigration(from = 3, to = 4),
    AutoMigration(from = 4, to = 5),
  ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun timelineDao(): TimelineDao
  abstract fun accountDao(): AccountDao
  abstract fun instanceDao(): InstanceDao
}
