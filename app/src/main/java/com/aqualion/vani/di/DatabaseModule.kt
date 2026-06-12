package com.aqualion.vani.di

import android.content.Context
import com.aqualion.vani.data.AppDatabase
import com.aqualion.vani.data.AppDatabaseHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return AppDatabaseHelper(applicationContext).getDatabaseInMemory()
    }
}