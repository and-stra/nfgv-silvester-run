package com.nfgv.stopwatch.data.service

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsReadDataApiResponse
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchTimestampsService @Inject constructor(
    private val googleSheetsRepository: GoogleSheetsRepository
) {
    suspend fun fetch(sheetsId: String, stopperId: String): GoogleSheetsReadDataApiResponse {
        return googleSheetsRepository.readValues(sheetsId, "$stopperId!A2:A")
    }
}