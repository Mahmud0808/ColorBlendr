package com.drdisagree.colorblendr.di

import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.repository.CustomStyleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCustomStyleRepository(): CustomStyleRepository =
        Utilities.getCustomStyleRepository()
}
