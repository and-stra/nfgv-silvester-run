package com.nfgv.stopwatch.di

import android.content.Context
import com.nfgv.stopwatch.data.repository.local.PreferencesRepository
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import com.nfgv.stopwatch.data.service.FetchRunDataService
import com.nfgv.stopwatch.data.service.ProvideGoogleSheetsClientService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {
    @Provides
    fun providePreferencesRepository(@ApplicationContext context: Context) = PreferencesRepository(context)

    @Provides
    fun provideGoogleSheetsRepository(
        provideGoogleSheetsClientService: ProvideGoogleSheetsClientService
    ) = GoogleSheetsRepository(provideGoogleSheetsClientService)
}