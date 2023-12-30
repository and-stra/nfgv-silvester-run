package com.nfgv.stopwatch.di

import android.content.Context
import com.nfgv.stopwatch.auth.service.FindLoggedInAccountService
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import com.nfgv.stopwatch.data.service.AddSheetService
import com.nfgv.stopwatch.data.service.FetchTimestampsService
import com.nfgv.stopwatch.data.service.FetchRunStartTimeService
import com.nfgv.stopwatch.data.service.ProvideGoogleSheetsClientService
import com.nfgv.stopwatch.data.service.PublishTimestampsService
import com.nfgv.stopwatch.data.service.UploadBackupDataService
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
    fun provideFetchRunStartTimeService(
        googleSheetsRepository: GoogleSheetsRepository
    ) = FetchRunStartTimeService(googleSheetsRepository)

    @Provides
    fun providePublishTimestampsService(
        googleSheetsRepository: GoogleSheetsRepository
    ) = PublishTimestampsService(googleSheetsRepository)

    @Provides
    fun provideFetchTimestampsService(
        googleSheetsRepository: GoogleSheetsRepository
    ) = FetchTimestampsService(googleSheetsRepository)

    @Provides
    fun provideAddSheetService(
        googleSheetsRepository: GoogleSheetsRepository
    ) = AddSheetService(googleSheetsRepository)

    @Provides
    fun provideUploadBackupDataService(
        googleSheetsRepository: GoogleSheetsRepository
    ) = UploadBackupDataService(googleSheetsRepository)
}