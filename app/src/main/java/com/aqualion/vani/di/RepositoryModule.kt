package com.aqualion.vani.di

import com.aqualion.vani.data.ProjectRepositoryImpl
import com.aqualion.vani.domain.ProjectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository
}