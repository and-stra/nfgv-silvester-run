package com.nfgv.stopwatch.data.service

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsPostApiResponse
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublishTimestampsService @Inject constructor(
    private val googleSheetsRepository: GoogleSheetsRepository
) {
    suspend fun publish(
        sheetsId: String,
        range: String,
        timestamps: List<List<Long>>
    ): GoogleSheetsPostApiResponse {
        return googleSheetsRepository.appendValues(sheetsId, range, timestamps)
    }
}