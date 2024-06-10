package com.github.whitescent.mastify.core.model

import Mastify.core.codegen.PreferencesFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PreferenceModule {
  @Provides
  @Singleton
  fun providePreferencesFactory() = PreferencesFactory()

  @Provides
  @Singleton
  fun provideAppData(preferencesFactory: PreferencesFactory) = preferencesFactory.appData
}
