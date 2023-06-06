package com.github.whitescent.mastify.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.whitescent.mastify.database.model.AccountEntity

@Dao
interface AccountDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertOrReplace(account: AccountEntity): Long

  @Delete
  fun delete(account: AccountEntity)

  @Query("SELECT * FROM AccountEntity ORDER BY id ASC")
  fun loadAll(): List<AccountEntity>
}
