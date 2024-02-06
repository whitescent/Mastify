/*
 * Copyright 2024 WhiteScent
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

package com.github.whitescent.mastify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DispatcherModule {

  @DefaultDispatcher
  @Provides
  fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

  @IoDispatcher
  @Provides
  fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

  @MainDispatcher
  @Provides
  fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@InstallIn(SingletonComponent::class)
@Module
class CoroutinesScopesModule {
  @Singleton
  @Provides
  @ApplicationScope
  fun providesCoroutineScope(
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
  ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher
