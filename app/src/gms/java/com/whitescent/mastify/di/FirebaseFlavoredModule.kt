package com.whitescent.mastify.di

import com.github.whitescent.mastify.data.repository.FirebaseRepository
import com.whitescent.mastify.data.repository.FirebaseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FirebaseFlavoredModule {
  @Binds
  fun bindFirebaseRepositoryImpl(firebaseRepositoryImpl: FirebaseRepositoryImpl): FirebaseRepository
}
