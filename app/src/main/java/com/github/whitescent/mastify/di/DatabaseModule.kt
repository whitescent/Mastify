package com.github.whitescent.mastify.di

import android.content.Context
import androidx.room.Room
import com.github.whitescent.mastify.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Provides
  @Singleton
  fun providesNiaDatabase(
    @ApplicationContext context: Context,
  ): AppDatabase = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "mastify-database",
  ).allowMainThreadQueries()
    .build()
}
