package com.github.whitescent.mastify.core.data

import com.github.whitescent.mastify.core.data.repository.PreferenceRepository
import com.github.whitescent.mastify.core.model.AppDataProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
  @Provides
  @Singleton
  fun provideJson() = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
  }

  @Provides
  @Singleton
  fun provideAppData(bind: PreferenceRepository): AppDataProvider = bind
}
