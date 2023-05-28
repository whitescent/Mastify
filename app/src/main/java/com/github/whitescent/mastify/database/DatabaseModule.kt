package com.github.whitescent.mastify.database

import android.content.Context
import androidx.room.Room
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
  ): MastifyDatabase = Room.databaseBuilder(
    context,
    MastifyDatabase::class.java,
    "mastify-database",
  ).build()
}
