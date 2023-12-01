package com.nfgv.stopwatch.di

import android.content.Context
import com.nfgv.stopwatch.auth.service.FindLoggedInAccountService
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
object ServiceModule {
    @Provides
    fun provideFindLoggedInAccountService(
        @ApplicationContext context: Context
    ) = FindLoggedInAccountService(context)

    @Provides
    fun provideGoogleSheetsClientProviderService(
        @ApplicationContext context: Context,
        findLoggedInAccountService: FindLoggedInAccountService
    ) = ProvideGoogleSheetsClientService(context, findLoggedInAccountService)

    @Provides
    fun provideFetchRunDataService(
        googleSheetsRepository: GoogleSheetsRepository
    ) = FetchRunDataService(googleSheetsRepository)
}