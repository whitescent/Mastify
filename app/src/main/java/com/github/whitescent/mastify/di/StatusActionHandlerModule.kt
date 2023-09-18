package com.github.whitescent.mastify.di

import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.network.MastodonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object StatusActionHandlerModule {
  @Provides
  fun provideStatusActionHandler(
    api: MastodonApi
  ): StatusActionHandler = StatusActionHandler(api)
}
