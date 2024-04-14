package com.github.whitescent.mastify.di

import com.github.whitescent.mastify.data.repository.FirebaseRepository
import com.github.whitescent.mastify.data.repository.FirebaseRepositoryStubImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FirebaseFlavoredModule {
  @Binds
  fun bindFirebaseRepositoryStubImpl(firebaseRepositoryStubImpl: FirebaseRepositoryStubImpl): FirebaseRepository
}
