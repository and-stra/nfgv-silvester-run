package com.nfgv.stopwatch.data.service

import com.nfgv.stopwatch.data.domain.response.GoogleSheetsGetApiResponse
import com.nfgv.stopwatch.data.repository.remote.GoogleSheetsRepository
import com.nfgv.stopwatch.util.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchRunDataService @Inject constructor(
    private val googleSheetsRepository: GoogleSheetsRepository
) {
    suspend fun fetch(sheetsId: String): GoogleSheetsGetApiResponse {
        return googleSheetsRepository.readValues(sheetsId, Constants.RUN_DATA_RANGE)
    }
}